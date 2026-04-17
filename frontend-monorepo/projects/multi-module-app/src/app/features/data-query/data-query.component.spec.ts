import { HttpClient } from '@angular/common/http';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';

import { BuiltinModules } from '../../core/config/data-query.config';
import { AuthService } from '../../core/services/auth.service';
import { DataQueryComponent } from './data-query.component';

describe('DataQueryComponent', () => {
  let http: jasmine.SpyObj<HttpClient>;
  let dialog: jasmine.SpyObj<MatDialog>;
  let component: DataQueryComponent;

  beforeEach(() => {
    http = jasmine.createSpyObj<HttpClient>('HttpClient', ['get', 'post']);
    dialog = jasmine.createSpyObj<MatDialog>('MatDialog', ['open']);

    component = new DataQueryComponent(http, dialog, {
      currentUserValue: {
        email: 'alice@example.com'
      }
    } as AuthService);

    component.config = BuiltinModules['xms'];
    component.queryTabs = [
      {
        title: 'All Data',
        dataSource: [
          {
            id: 1,
            tradeType: 'Spot'
          }
        ],
        colDefs: component.config.colDefs
      }
    ];
    component.selectedTabIndex = 0;
    component.gridApi = {
      getSelectedNodes: jasmine.createSpy().and.returnValue([]),
      getDisplayedRowCount: jasmine.createSpy().and.returnValue(1)
    };
  });

  it('shows inline feedback when export is triggered without selected rows', () => {
    component.exportSelectedRowsAndSendEmail();

    expect(http.post).not.toHaveBeenCalled();
    expect(component.exportFeedback).toEqual({
      tone: 'error',
      message: 'Select at least one row from the current result before exporting.'
    });
  });

  it('opens result tools by default and toggles them closed and open again', () => {
    expect(component.isResultToolsOpen).toBeTrue();

    component.toggleResultTools();
    expect(component.isResultToolsOpen).toBeFalse();

    component.toggleResultTools();
    expect(component.isResultToolsOpen).toBeTrue();
  });

  it('shows inline feedback when no export format is selected', () => {
    component.gridApi.getSelectedNodes.and.returnValue([
      {
        data: {
          id: 1,
          tradeType: 'Spot'
        }
      }
    ]);
    component.exportCsvSelected = false;
    component.exportXlsxSelected = false;

    component.exportSelectedRowsAndSendEmail();

    expect(http.post).not.toHaveBeenCalled();
    expect(component.exportFeedback).toEqual({
      tone: 'error',
      message: 'Select at least one file format before downloading or emailing the export.'
    });
  });

  it('uses the current user email when the recipient field is blank', async () => {
    component.gridApi.getSelectedNodes.and.returnValue([
      {
        data: {
          id: 1,
          tradeType: 'Spot'
        }
      }
    ]);

    spyOn<any>(component, 'downloadBlob');
    spyOn<any>(component, 'blobToBase64').and.resolveTo('encoded-file');
    http.post.and.returnValue(of({ deliveryMode: 'log-only' }));

    component.exportSelectedRowsAndSendEmail();
    await new Promise(resolve => setTimeout(resolve, 0));

    expect(http.post).toHaveBeenCalledWith(
      `${component.config.apiEndpoint}/export/email`,
      jasmine.objectContaining({
        recipients: ['alice@example.com'],
        attachments: [
          jasmine.objectContaining({
            format: 'csv',
            fileBase64: 'encoded-file'
          })
        ],
        rowCount: 1
      })
    );
    expect(component.exportFeedback).toEqual({
      tone: 'success',
      message:
        'CSV downloaded and export email accepted for 1 recipient. Email delivery is running in local log-only mode.'
    });
  });

  it('downloads and sends both csv and xlsx attachments when both formats are selected', async () => {
    component.gridApi.getSelectedNodes.and.returnValue([
      {
        data: {
          id: 1,
          tradeType: 'Spot'
        }
      }
    ]);
    component.exportXlsxSelected = true;

    const downloadBlobSpy = spyOn<any>(component, 'downloadBlob');
    spyOn<any>(component, 'blobToBase64').and.resolveTo('encoded-file');
    http.post.and.returnValue(of({ status: 'accepted', deliveryMode: 'log-only' }));

    component.exportSelectedRowsAndSendEmail();
    await new Promise(resolve => setTimeout(resolve, 0));

    expect(downloadBlobSpy).toHaveBeenCalledTimes(2);
    expect(http.post).toHaveBeenCalledWith(
      `${component.config.apiEndpoint}/export/email`,
      jasmine.objectContaining({
        attachments: [
          jasmine.objectContaining({ format: 'csv', fileName: jasmine.stringMatching(/\.csv$/) }),
          jasmine.objectContaining({ format: 'xlsx', fileName: jasmine.stringMatching(/\.xlsx$/) })
        ]
      })
    );
    expect(component.exportFeedback).toEqual({
      tone: 'success',
      message:
        'CSV and XLSX downloaded and export email accepted for 1 recipient. Email delivery is running in local log-only mode.'
    });
  });
});
