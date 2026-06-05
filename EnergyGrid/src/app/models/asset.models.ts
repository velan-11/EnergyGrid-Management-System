export interface Asset {
  AssetID: number;
  Name?: string | null;
  Type: string;
  Location: string;
  CapacityKW: number;
  CommissionedAt?: string | null;
  Status: string;
  // lowercase fallback names (backend may return either casing)
  assetId?: number;
  name?: string | null;
  asset_name?: string | null;
  type?: string;
  location?: string;
  capacityKW?: number;
  capacity_kw?: number;
  status?: string;
}

export type AssetType = 'Solar' | 'Wind' | 'Hydro' | 'Battery' | 'Meter';
export type AssetStatus = 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE' | 'RETIRED';

export interface AssetRequest {
  Name?: string | null;
  Type: string;
  Location: string;
  CapacityKW: number;
  CommissionedAt?: string | null;
  Status: string;
}

// Helper functions to read asset fields regardless of which casing the API returns
export function assetId(a: Asset): number {
  return a.AssetID || a.assetId || 0;
}

export function assetName(a: Asset): string {
  return String(a.Name || a.name || a.asset_name || '');
}

export function assetType(a: Asset): string {
  return String(a.Type || a.type || '');
}

export function assetLocation(a: Asset): string {
  return String(a.Location || a.location || '');
}

export function assetCapacity(a: Asset): number {
  return Number(a.CapacityKW || a.capacityKW || a.capacity_kw || 0);
}

export function assetStatus(a: Asset): string {
  return String(a.Status || a.status || '');
}

export function assetDisplayName(a: Asset): string {
  const n = assetName(a).trim();
  if (n) return n;
  return (assetType(a) || 'Asset') + ' #' + (assetId(a) || '?');
}

export function assetNameWithType(a: Asset): string {
  const n = assetName(a).trim();
  const t = assetType(a) || 'Asset';
  if (n) return n + ' — ' + t;
  return t + ' #' + (assetId(a) || '?');
}
