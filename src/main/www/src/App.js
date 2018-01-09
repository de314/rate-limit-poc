import React from 'react'
import axios from 'axios'
import classnames from 'classnames'

import { compose, withHandlers, withState } from 'recompose'

import Select from 'react-select'
import RunConfigsForm from './RunConfigsForm'

const exeDurationOptions = window.exeDurationOptions

const App = ({ runConfigs, setConfig, disabled, start }) => (
  <div className="App">
    <div className="container">
      <div className="h1">Input</div>
      <RunConfigsForm runConfigs={runConfigs} setConfig={setConfig} disabled={disabled} />
      <button className="btn btn-success w-100 mt-3" onClick={start} disabled={disabled}>
        <i className="fa fa-play" /> Run
      </button>
      <hr className="m-3" />
      <div className="h1">Queue</div>
      <hr className="m-3" />
      <div className="h1">Results</div>
    </div>
  </div>
)

const defaultRunConfigs = {
  concurrency: 10,
  count: 100,
  exeDurr: exeDurationOptions.slice(1, 3),
}

export default compose(
  withState('runConfigs', 'setRunConfigs', defaultRunConfigs),
  withState('concurrency', 'setConcurrency', 10),
  withState('count', 'setCount', 100),
  withState('disabled', 'setDisabled', false),
  withHandlers({
    setConfig: ({ runConfigs, setRunConfigs }) => (key, val) =>
      setRunConfigs({ ...runConfigs, [key]: val }),
    start: ({ runConfigs, disabled, setDisabled }) => () => {
      const { concurrency, count, exeDurr } = runConfigs
      if (disabled) {
        console.error('ALREADY RUNNING')
      } else {
        setDisabled(true)
        setTimeout(() => setDisabled(false), 2500)
        console.log(
          `Starting with ${concurrency} runners making ${count} requests with [${exeDurr.map(
            item => item.value
          )}] params`
        )
      }
    },
  })
)(App)
