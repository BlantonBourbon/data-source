import { ColDef } from 'ag-grid-community';

export type FilterFieldType = 'string' | 'number' | 'date' | 'time' | 'checkbox' | 'dropdown';

import { ModuleDefinition } from '../models/auth.models';

export interface MockQueryRow {
  [key: string]: string | number | boolean | null;
}

export interface FilterField {
  name: string;
  label: string;
  type: FilterFieldType;
  checkboxOptions?: Array<{ label: string; value: string }>;
  dropdownOptions?: Array<{ label: string; value: string }>;
  group?: string;
  gridSpan?: 3 | 4 | 6 | 12;
  placeholder?: string;
  mockOptions?: string[];
}

export interface DataQueryConfig extends ModuleDefinition {
  apiEndpoint: string;
  metricEndpoint: string;
  colDefs: ColDef[];
  filterFields: FilterField[];
  numericColumns: string[];
  groupByFields: string[];
  mockData?: MockQueryRow[];
}

const entityApiBasePath = '/api/user';

export const BuiltinModules: Record<string, DataQueryConfig> = {
  xms: {
    id: 'xms',
    name: 'XMS Module (Trades)',
    description: 'Query and view XMS Trades data.',
    bgStyle: 'linear-gradient(135deg, #1b539c 0%, #089fd1 100%)',
    logo: 'XMS',
    authorization: {
      permissions: ['module:xms:read'],
      match: 'all'
    },
    apiEndpoint: `${entityApiBasePath}/trades`,
    metricEndpoint: `${entityApiBasePath}/trades/metric`,
    filterFields: [
      {
        name: 'tradeType',
        label: 'Trade Type',
        type: 'dropdown',
        group: 'Basic',
        mockOptions: ['SPOT', 'FORWARD', 'SWAP']
      },
      {
        name: 'currency',
        label: 'Currency',
        type: 'dropdown',
        group: 'Basic',
        mockOptions: ['USD', 'EUR', 'JPY']
      },
      { name: 'tradeDate', label: 'Trade Date', type: 'date', group: 'Time' },
      { name: 'counterparty', label: 'Counterparty', type: 'string', group: 'Counterparty' }
    ],
    numericColumns: ['amount', 'id'],
    groupByFields: ['tradeType', 'currency', 'counterparty'],
    mockData: [
      {
        id: 1,
        tradeType: 'SPOT',
        tradeDate: '2026-01-15',
        amount: 125000,
        currency: 'USD',
        counterparty: 'Alpha Capital'
      },
      {
        id: 2,
        tradeType: 'FORWARD',
        tradeDate: '2026-02-03',
        amount: 98000,
        currency: 'EUR',
        counterparty: 'Northwind Markets'
      },
      {
        id: 3,
        tradeType: 'SWAP',
        tradeDate: '2026-02-18',
        amount: 210500,
        currency: 'JPY',
        counterparty: 'Blue Ocean Fund'
      },
      {
        id: 4,
        tradeType: 'SPOT',
        tradeDate: '2026-03-01',
        amount: 75000,
        currency: 'USD',
        counterparty: 'Northwind Markets'
      }
    ],
    colDefs: [
      {
        field: 'id',
        headerName: 'ID',
        filter: 'agNumberColumnFilter',
        maxWidth: 100
      },
      { field: 'tradeType', headerName: 'Trade Type', filter: 'agTextColumnFilter' },
      { field: 'tradeDate', headerName: 'Trade Date', filter: 'agDateColumnFilter' },
      {
        field: 'amount',
        headerName: 'Amount',
        filter: 'agNumberColumnFilter',
        type: 'numericColumn',
        valueFormatter: params => (params.value != null ? Number(params.value).toLocaleString() : '')
      },
      { field: 'currency', headerName: 'Currency', filter: 'agTextColumnFilter', maxWidth: 150 },
      { field: 'counterparty', headerName: 'Counterparty', filter: 'agTextColumnFilter' }
    ]
  },
  libra: {
    id: 'libra',
    name: 'Libra Module (Assets)',
    description: 'Query and view CryptoAssets data.',
    bgStyle: 'linear-gradient(135deg, #8e2de2 0%, #4a00e0 100%)',
    logo: 'LIB',
    authorization: {
      permissions: ['module:libra:read'],
      match: 'all'
    },
    apiEndpoint: `${entityApiBasePath}/cryptoassets`,
    metricEndpoint: `${entityApiBasePath}/cryptoassets/metric`,
    filterFields: [
      {
        name: 'symbol',
        label: 'Symbol',
        type: 'dropdown',
        group: 'Basic',
        mockOptions: ['BTC', 'ETH', 'SOL']
      },
      { name: 'listingDate', label: 'Listing Date', type: 'date', group: 'Time' }
    ],
    numericColumns: ['marketCap', 'id'],
    groupByFields: ['symbol'],
    mockData: [
      {
        id: 1,
        symbol: 'BTC',
        marketCap: 1350000000000,
        listingDate: '2009-01-03'
      },
      {
        id: 2,
        symbol: 'ETH',
        marketCap: 420000000000,
        listingDate: '2015-07-30'
      },
      {
        id: 3,
        symbol: 'SOL',
        marketCap: 68000000000,
        listingDate: '2020-03-16'
      }
    ],
    colDefs: [
      {
        field: 'id',
        headerName: 'ID',
        filter: 'agNumberColumnFilter',
        maxWidth: 100
      },
      { field: 'symbol', headerName: 'Symbol', filter: 'agTextColumnFilter' },
      {
        field: 'marketCap',
        headerName: 'Market Cap',
        filter: 'agNumberColumnFilter',
        type: 'numericColumn',
        valueFormatter: params => (params.value != null ? Number(params.value).toLocaleString() : '')
      },
      { field: 'listingDate', headerName: 'Listing Date', filter: 'agDateColumnFilter' }
    ]
  }
};
