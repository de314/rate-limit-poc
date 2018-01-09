import React from 'react'
import classnames from 'classnames'

import { compose, withHandlers, withProps, withState } from 'recompose'

import './RunnersList.css'

let RunnerTile = ({ runner, isRunning, start, progress, remove }) => (
  <div className={classnames('RunnerTile col p-3', { running: isRunning })}>
    <div
      className="p-3"
      style={{
        maxWidth: '350px',
        border: '1px solid #ccc',
        position: 'relative',
      }}
    >
      <button
        className="btn btn-sm btn-danger"
        style={{ position: 'absolute', top: 5, right: 5 }}
        onClick={() => remove(runner)}
      >
        <i className="fa fa-trash" />
      </button>
      <div>
        <b>Id:</b> <code>{runner.id}</code>
      </div>
      <div>
        <b>Concurrency:</b> <code>{runner.runConfig.concurrency}</code>
      </div>
      <div>
        <b>Total Requests:</b> <code>{runner.runConfig.count}</code>
      </div>
      <div>
        <b>Requests:</b>
        <ul className="ml-3">
          {runner.runConfig.exeDurr.map((req, i) => (
            <li key={i}>
              {req.label}: <code>{req.value}</code>
            </li>
          ))}
        </ul>
      </div>
      <div>
        <button
          className="btn btn-success w-100"
          onClick={() => start(runner)}
          disabled={isRunning}
        >
          <i className="fa fa-play" /> Run
        </button>
      </div>

      {isRunning && (
        <div className="progress mt-2">
          <div
            className="progress-bar"
            role="progressbar"
            style={{ width: `${progress.percentageInt}%` }}
            aria-valuenow={progress.percentageInt}
            aria-valuemin="0"
            aria-valuemax="100"
          >
            {progress.percentageInt}%
          </div>
        </div>
      )}
    </div>
  </div>
)

RunnerTile = compose(
  withState('progress', 'setProgress', 0),
  withProps(({ runner }) => ({ isRunning: runner.isRunning() })),
  withHandlers({
    start: ({ isRunning, setProgress, addResults }) => runner => {
      if (!isRunning) {
        const intervalHandle = setInterval(
          () => setProgress(runner.getProgress()),
          250,
        )
        runner.start().then(results => {
          addResults(results)
          clearInterval(intervalHandle)
        })
      }
    },
  }),
)(RunnerTile)

const RunnersList = ({ runners, removeRunner, addResults }) => (
  <div className="RunnersList row">
    {runners.length === 0 ? (
      <div className="h4 text-muted">There is nothing here...</div>
    ) : (
      runners.map(r => (
        <RunnerTile
          runner={r}
          remove={removeRunner}
          addResults={addResults}
          key={r.id}
        />
      ))
    )}
  </div>
)

export default RunnersList
