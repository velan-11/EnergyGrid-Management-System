-- =====================================================================
-- V2 — canonicalise work_orders.status
--
-- The service originally wrote "CREATED" on new rows and "ASSIGNED" when a
-- technician was attached. Neither value matched the frontend badge palette
-- (OPEN / IN_PROGRESS / COMPLETED / CANCELLED) so the column rendered as a
-- neutral pill with no colour cue.  This migration flips every existing row
-- to the canonical enum the service now writes.
--
-- Safe to re-run (UPDATEs are idempotent against the canonical set).
-- =====================================================================

UPDATE work_orders
SET    status = 'OPEN'
WHERE  status IS NULL
   OR  status = ''
   OR  UPPER(status) = 'CREATED';

UPDATE work_orders
SET    status = 'IN_PROGRESS'
WHERE  UPPER(status) = 'ASSIGNED';

-- Any other free-text value (e.g. 'pending', 'draft') falls back to OPEN so
-- the UI never renders an unknown-coloured pill.
UPDATE work_orders
SET    status = 'OPEN'
WHERE  UPPER(status) NOT IN ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');
