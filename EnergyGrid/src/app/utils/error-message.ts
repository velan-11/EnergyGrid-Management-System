export function extractErrorMessage(err: any, fallback: string): string {
  if (!err) return fallback;

  const body = err.error;
  if (typeof body === 'string' && body.trim()) return body.trim();

  if (body && typeof body === 'object') {
    const fieldMessage = readFieldMessages(body);
    if (fieldMessage) return fieldMessage;

    if (typeof body.message === 'string' && body.message) return body.message;
    if (typeof body.error === 'string' && body.error && body.error !== 'VALIDATION_FAILED') return body.error;
    if (typeof body.detail === 'string' && body.detail) return body.detail;
  }

  if (typeof err.message === 'string' && err.message) return err.message;
  return fallback;
}

function readFieldMessages(body: any): string | null {
  const messages = body.messages || body.fieldErrors || body.errors;
  if (!messages || typeof messages !== 'object' || Array.isArray(messages)) return null;

  const pairs: string[] = [];
  for (const key in messages) {
    if (typeof messages[key] === 'string' && messages[key]) {
      pairs.push(key + ': ' + messages[key]);
    }
  }
  return pairs.length > 0 ? pairs.join('; ') : null;
}
