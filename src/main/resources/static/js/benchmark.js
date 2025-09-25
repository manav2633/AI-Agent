document.getElementById('benchmark-form').addEventListener('submit', async e=>{
    e.preventDefault();
    const name = document.getElementById('bench-name').value.trim();
    const taskId = +document.getElementById('bench-task-id').value;
    const iters = +document.getElementById('bench-iters').value;
    const frameworks = Array.from(
      document.getElementById('bench-frameworks').selectedOptions
    ).map(o=>o.value);
    const res = await fetch('/api/benchmarks/execute',{
      method:'POST', headers:{'Content-Type':'application/json'},
      body:JSON.stringify({ name, taskId, frameworkTypes:frameworks, iterations:iters })
    });
    const out=document.getElementById('benchmark-result');
    out.textContent = res.status===202?'Benchmark started':'Failed to start';
    loadActiveBenchmarks();
  });
  
  document.getElementById('btn-active-benchmarks').addEventListener('click',
    loadActiveBenchmarks);
  
  async function loadActiveBenchmarks() {
    const res = await fetch('/api/benchmarks/runs/active');
    if(!res.ok) return;
    const data=await res.json();
    const c=document.getElementById('active-benchmarks');
    c.innerHTML='';
    Object.entries(data).forEach(([runId,run])=>{
      const d=document.createElement('div');
      d.textContent=`${runId} | ${run.status} | ${run.completedExecutions}/${run.totalExecutions}`;
      c.append(d);
    });
  }
  