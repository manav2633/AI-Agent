function initDashboard(frameworkComparison, systemSummary) {
  console.log('Dashboard initialized with data:', { frameworkComparison, systemSummary });
  
  // Initialize comparison chart if data exists
  if (frameworkComparison && frameworkComparison.length > 0) {
      initComparisonChart(frameworkComparison);
  }
  
  // Setup auto-refresh every 30 seconds
  setInterval(() => {
      location.reload();
  }, 30000);
}

function initComparisonChart(data) {
  const ctx = document.getElementById('comparisonChart');
  if (!ctx) return;
  
  const labels = data.map(d => d.frameworkType);
  const successRates = data.map(d => d.successRate || 0);
  const avgTimes = data.map(d => d.averageResponseTimeMs || 0);
  
  new Chart(ctx, {
      type: 'bar',
      data: {
          labels: labels,
          datasets: [
              {
                  label: 'Success Rate (%)',
                  data: successRates,
                  backgroundColor: 'rgba(37, 99, 235, 0.8)',
                  yAxisID: 'y'
              },
              {
                  label: 'Avg Response Time (ms)',
                  data: avgTimes,
                  backgroundColor: 'rgba(16, 185, 129, 0.8)',
                  yAxisID: 'y1'
              }
          ]
      },
      options: {
          responsive: true,
          scales: {
              y: {
                  type: 'linear',
                  display: true,
                  position: 'left',
                  max: 100,
                  title: { display: true, text: 'Success Rate (%)' }
              },
              y1: {
                  type: 'linear',
                  display: true,
                  position: 'right',
                  title: { display: true, text: 'Response Time (ms)' },
                  grid: { drawOnChartArea: false }
              }
          }
      }
  });
}
