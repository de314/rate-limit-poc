import React from 'react'
import _ from 'lodash'

import { withHandlers } from 'recompose'

import Select from 'react-select'

const exeDurationOptions = window.exeDurationOptions

const methods = [{ label: 'GET', value: 'get' }]

const RunConfigsForm = ({
  runConfigs,
  setConfig,
  disabled,
  setDefConfig,
  copyDef,
}) => (
  <div className="RunConfigsForm">
    <div className="form-group">
      <div className="h3">Name</div>
      <input
        type="text"
        className="form-control form-control-lg"
        value={runConfigs.name}
        onChange={e => setConfig('name', e.target.value)}
      />
    </div>
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
    </div>
    <div className="form-group">
      <div className="h3">Requests Definitions</div>
      <table className="table table-striped table-hover">
        <thead>
          <tr>
            <th>Active</th>
            <th>Method</th>
            <th>Url</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {runConfigs.reqDefs.map((def, i) => (
            <tr key={i}>
              <td>
                <input
                  type="checkbox"
                  className="form-control"
                  checked={def.active}
                  onChange={e => setDefConfig(i, 'active', e.target.checked)}
                />
              </td>
              <td>
                <Select
                  value={def.method}
                  onChange={value => setDefConfig(i, 'method', value)}
                  options={methods}
                  simpleValues
                />
              </td>
              <td>
                <input
                  type="text"
                  className="form-control"
                  value={def.url}
                  onChange={e => setDefConfig(i, 'url', e.target.value)}
                />
              </td>
              <td>
                <button
                  className="btn btn-sm btn-primary"
                  onClick={() => copyDef(i)}
                >
                  <i className="fa fa-copy" />
                </button>
                <button className="btn btn-sm btn-danger">
                  <i className="fa fa-trash" />
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  </div>
)

export default withHandlers({
  setDefConfig: ({ runConfigs, setConfig }) => (i, key, value) => {
    runConfigs.reqDefs[i][key] = value
    setConfig('reqDefs', runConfigs.reqDefs)
  },
  copyDef: ({ runConfigs, setConfig }) => i => {
    runConfigs.reqDefs.push(_.cloneDeep(runConfigs.reqDefs[i]))
    setConfig('reqDefs', runConfigs.reqDefs)
  },
})(RunConfigsForm)
