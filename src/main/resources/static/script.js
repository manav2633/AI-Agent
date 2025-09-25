console.log('Frontend initialized');
window.addEventListener('focus',()=>{
  if(typeof loadExecutions==='function') loadExecutions();
  if(typeof loadMetricsSummary==='function') loadMetricsSummary();
});
