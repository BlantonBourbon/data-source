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
    http.post.and.returnValue(of({}));

    component.exportSelectedRowsAndSendEmail();
    await new Promise(resolve => setTimeout(resolve, 0));

    expect(http.post).toHaveBeenCalledWith(
      `${component.config.apiEndpoint}/export/email`,
      jasmine.objectContaining({
        recipients: ['alice@example.com'],
        fileBase64: 'encoded-file',
        rowCount: 1
      })
    );
    expect(component.exportFeedback).toEqual({
      tone: 'success',
      message: 'CSV downloaded and email queued for 1 recipient.'
    });
  });
});
