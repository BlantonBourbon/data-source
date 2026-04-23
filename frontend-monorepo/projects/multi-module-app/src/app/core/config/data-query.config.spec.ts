import { BuiltinModules } from './data-query.config';

describe('BuiltinModules', () => {
  it('uses human audience routes for xms', () => {
    expect(BuiltinModules['xms'].apiEndpoint).toBe('/api/user/trades');
    expect(BuiltinModules['xms'].metricEndpoint).toBe('/api/user/trades/metric');
  });

  it('uses human audience routes for libra', () => {
    expect(BuiltinModules['libra'].apiEndpoint).toBe('/api/user/cryptoassets');
    expect(BuiltinModules['libra'].metricEndpoint).toBe('/api/user/cryptoassets/metric');
  });

  it('configures dropdown filters with mock support for xms', () => {
    expect(BuiltinModules['xms'].filterFields.find(field => field.name === 'tradeType')?.type).toBe('dropdown');
    expect(BuiltinModules['xms'].filterFields.find(field => field.name === 'currency')?.type).toBe('dropdown');
    expect(BuiltinModules['xms'].mockData?.length).toBeGreaterThan(0);
  });

  it('configures dropdown filters with mock support for libra', () => {
    expect(BuiltinModules['libra'].filterFields.find(field => field.name === 'symbol')?.type).toBe('dropdown');
    expect(BuiltinModules['libra'].mockData?.length).toBeGreaterThan(0);
  });
});
