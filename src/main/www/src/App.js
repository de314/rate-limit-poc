import React from 'react'
import PerfRunner from './PerfRunner'

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

const defaultRunConfigs = {
  concurrency: 10,
  count: 30,
  exeDurr: exeDurationOptions.slice(1, 2), //exeDurationOptions.slice(1, 3),
}

const defaultRunner = new PerfRunner(defaultRunConfigs)

export default compose(
  withState('runConfigs', 'setRunConfigs', defaultRunConfigs),
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
      // a little debounce action... which is probably not needed
      if (!disabled) {
        setDisabled(true)
        setTimeout(() => setDisabled(false), 750)
        setPerfRunners([new PerfRunner(runConfigs), ...perfRunners])
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
