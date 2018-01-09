import React from 'react'
import moment from 'moment'

/*
"summary": {
    "totalReqTime": 12814,
    "totalReqs": 30,
    "minReqDurr": 222,
    "maxReqDurr": 562,
    "meanDur": 427.1333333333333
  }
*/

const formatNum = num => num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')

const ResultsTile = ({ result }) => (
  <div
    className="ResultsTile col p-3"
    style={{
      maxWidth: '350px',
      border: '1px solid #ccc',
      position: 'relative',
    }}
  >
    <div>
      <b>Run Id:</b> {result.runId}
    </div>
    <div>
      <b>Started:</b> {moment(result.startTime).format('lll')}
    </div>
    <hr />
    <div>
      <em>
        <b>Summary:</b>
      </em>
    </div>
    <div>
      <b>Total Request:</b> {formatNum(result.summary.totalReqs)}
    </div>
    <div>
      <b>Total Network Time:</b> {formatNum(result.summary.totalReqTime)} ms ({moment.duration(result.summary.totalReqTime).humanize()})
    </div>
    <div>
      <b>Min Duration:</b> {formatNum(result.summary.minReqDurr)}ms
    </div>
    <div>
      <em>
        <b>Mean Duration:</b> {formatNum(result.summary.meanDur)}ms
      </em>
    </div>
    <div>
      <b>Max Duration:</b> {formatNum(result.summary.maxReqDurr)}ms
    </div>
  </div>
)

const ResultsList = ({ results }) => (
  <div className="ResultsList">
    <div className="row">
      {results.map(r => <ResultsTile result={r} key={r.runId} />)}
    </div>
  </div>
)

export default ResultsList
