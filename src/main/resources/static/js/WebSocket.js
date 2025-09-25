const WS_URL = `${location.protocol === 'https:'?'wss':'ws'}://${location.host}/api/ws`;
let ws;

function connectWS() {
  ws = new WebSocket(WS_URL);
  ws.addEventListener('open', () => {
    console.log('[WS] connected');
    ws.send(JSON.stringify({ type: 'ping', timestamp: Date.now() }));
  });
  ws.addEventListener('message', e => {
    const msg = JSON.parse(e.data);
    if (msg.type === 'EXECUTION_UPDATE') {
      appendExecutionItem({
        id: msg.executionId,
        frameworkType: msg.frameworkType,
        status: msg.status,
        executionDurationMs: msg.duration
      }, true);
    }
    if (msg.type === 'BENCHMARK_UPDATE') {
      document.getElementById('benchmark-result').textContent =
        `Benchmark ${msg.name||msg.benchmarkRunId} - ${msg.status}`;
    }
    if (msg.type === 'METRICS_UPDATE') {
      loadMetricsSummary();
    }
  });
  ws.addEventListener('close', () => {
    console.log('[WS] disconnected, retrying…');
    setTimeout(connectWS, 3000);
  });
  ws.addEventListener('error', e => {
    console.warn('[WS] error', e);
    ws.close();
  });
}

connectWS();
