import React from 'react'

import Select from 'react-select'

const exeDurationOptions = window.exeDurationOptions

const RunConfigsForm = ({ runConfigs, setConfig, disabled }) => (
  <div className="RunConfigsForm">
    <div className="row">
      <div className="col">
        <div className="h3">Concurrency</div>
        <input
          type="number"
          className="form-control"
          min="1"
          max="100"
          value={runConfigs.concurrency}
          onChange={e => setConfig('concurrency', e.target.value)}
          disabled={disabled}
        />
        <span className="text-muted">
          <em>range: [1, 10000]</em>
        </span>
      </div>
      <div className="col">
        <div className="h3">Total Requests</div>
        <input
          type="number"
          className="form-control"
          min="1"
          max="10000"
          value={runConfigs.count}
          onChange={e => setConfig('count', e.target.value)}
          disabled={disabled}
        />
        <span className="text-muted">
          <em>range: [1, 10000]</em>
        </span>
      </div>
      <div className="col">
        <div className="h3">Expected Response Time</div>
        <Select
          value={runConfigs.exeDurr}
          onChange={v => setConfig('exeDurr', v)}
          multi={true}
          options={exeDurationOptions}
          disabled={disabled}
        />
      </div>
    </div>
  </div>
)

export default RunConfigsForm
