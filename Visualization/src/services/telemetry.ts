export type TelemetryPayload = {
  type: string;
  ts: number;
  payload: Record<string, unknown>;
};

const send = async (data: TelemetryPayload) => {
  try {
    await fetch('/api/telemetry', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(data),
    });
  } catch (err) {
    console.warn('telemetry send failed', err);
  }
};

export const initTelemetry = () => {
  const log = (type: string, payload: Record<string, unknown>) => {
    void send({ type, ts: Date.now(), payload });
  };

  document.addEventListener('click', (e) => {
    const target = e.target as HTMLElement | null;
    log('click', {
      tag: target?.tagName,
      id: target?.id,
      classes: target?.className,
      text: target?.innerText?.slice(0, 120),
    });
  });

  window.addEventListener('error', (e) => {
    log('error', {
      message: e.message,
      source: e.filename,
      line: e.lineno,
      col: e.colno,
    });
  });

  window.addEventListener('unhandledrejection', (e) => {
    log('promise-rejection', {
      reason: typeof e.reason === 'string' ? e.reason : JSON.stringify(e.reason ?? {}),
    });
  });
};
