async function loadMetricsSummary(){
    const res=await fetch('/api/metrics/system/summary');
    if(!res.ok) return;
    const s=await res.json();
    document.getElementById('metrics-summary').innerHTML = `
      <p>Success Rate: ${s.overallSuccessRate?.toFixed(2)||0}%</p>
      <p>Avg Response: ${Math.round(s.overallAverageResponseTime||0)} ms</p>
      <p>Consistency: ${s.overallConsistencyScore?.toFixed(2)||0}%</p>
    `;
  }
  
  let chart;
  async function loadComparisonChart(){
    const res=await fetch('/api/metrics/comparison');
    if(!res.ok) return;
    const data=await res.json();
    const labels=data.map(d=>d.frameworkType);
    const rates=data.map(d=>d.successRate);
    const ctx=document.getElementById('comparison-chart').getContext('2d');
    if(chart) chart.destroy();
    chart=new Chart(ctx,{
      type:'bar',
      data:{labels,datasets:[{label:'Success Rate',data:rates,backgroundColor:'#3498db'}]},
      options:{scales:{y:{beginAtZero:true,max:100}}}
    });
    fillSidePanels(data);
  }
  
  async function fillSidePanels(data){
    const top = [...data].sort((a,b)=>b.successRate-a.successRate).slice(0,3);
    const topList=document.getElementById('top-performers');
    topList.innerHTML='';
    top.forEach(t=>{
      const li=document.createElement('li');
      li.textContent=`${t.frameworkType}: ${t.successRate.toFixed(2)}%`;
      topList.append(li);
    });
    const distRes=await fetch('/api/metrics/reliability/distribution');
    const dist=distRes.ok?await distRes.json():{};
    const distList=document.getElementById('reliability-dist');
    distList.innerHTML='';
    ['Excellent','Good','Fair','Poor'].forEach(b=>{
      const li=document.createElement('li');
      li.textContent=`${b}: ${dist[b]||0}`;
      distList.append(li);
    });
  }
  
  loadMetricsSummary();
  loadComparisonChart();
  