import React from 'react'
import _ from 'lodash'
import PerfRunner from './PerfRunner'
import uuid from 'uuid'

import { compose, withHandlers, withState } from 'recompose'

import RunConfigsForm from './RunConfigsForm'
import RunnersList from './RunnersList'
import ResultsList from './ResultsList'

const exeDurationOptions = window.exeDurationOptions

const App = ({
  runConfigs,
  setConfig,
  disabled,
  perfRunners,
  addPerfRunner,
  removePerfRunner,
  addResults,
  results,
  removeResult,
}) => (
  <div className="App">
    <div className="container">
      <div className="h1">Input</div>
      <RunConfigsForm
        runConfigs={runConfigs}
        setConfig={setConfig}
        disabled={disabled}
      />
      <button
        className="btn btn-primary w-100 mt-3"
        onClick={addPerfRunner}
        disabled={disabled}
      >
        <i className="fa fa-plus" /> Add New Runner
      </button>
      <hr className="m-3" />
      <div className="h1">Runners</div>
      <RunnersList
        runners={perfRunners}
        removeRunner={removePerfRunner}
        addResults={addResults}
      />
      <hr className="m-3" />
      <div className="h1">Results</div>
      <ResultsList results={results} removeResult={removeResult} />
    </div>
  </div>
)

const defaultRunConfigs = (
  name = `Runner #${Math.ceil(Math.random() * 1000)}`,
) => ({
  name,
  concurrency: 10,
  count: 30,
  reqDefs: [
      { active: true, method: 'get', url: 'http://localhost:9090/fib/calc/40' },
      { active: true, method: 'get', url: 'http://localhost:9090/rl/fib/calc/40' },
  ],
})

const defaultRunner = new PerfRunner(defaultRunConfigs())

export default compose(
  withState('runConfigs', 'setRunConfigs', defaultRunConfigs()),
  withState('disabled', 'setDisabled', false),
  withState('perfRunners', 'setPerfRunners', [defaultRunner]),
  withState('results', 'setResults', []),
  withHandlers({
    setConfig: ({ runConfigs, setRunConfigs }) => (key, val) =>
      setRunConfigs({ ...runConfigs, [key]: val }),
    addPerfRunner: ({
      runConfigs,
      disabled,
      setDisabled,
      perfRunners,
      setPerfRunners,
    }) => () => {
      const config = _.cloneDeep(runConfigs)
      config.reqDefs = config.reqDefs.filter(r => r.active)
      // a little debounce action... which is probably not needed
      if (!disabled && config.reqDefs.length !== 0) {
        setDisabled(true)
        setTimeout(() => setDisabled(false), 750)
        setPerfRunners([new PerfRunner(config), ...perfRunners])
      }
    },
    removePerfRunner: ({ perfRunners, setPerfRunners }) => runner => {
      const index = perfRunners.indexOf(runner)
      if (index >= 0) {
        perfRunners.splice(index, 1)
        setPerfRunners([...perfRunners])
      }
    },
    addResults: ({ results, setResults }) => newResults =>
      setResults([newResults, ...results]),
    removeResult: () => () => {},
  }),
)(App)
