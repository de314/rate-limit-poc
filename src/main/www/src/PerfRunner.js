import _ from 'lodash'
import axios from 'axios'
import uuid from 'uuid'

class Runner {
  constructor(count, requests, id) {
    this.id = id
    this.count = count
    this.index = 0
    this.requests = requests
    this.running = false
    this.promise = null
    this.results = []
  }

  _request() {
    const startTime = new Date().getTime()
    const request = this.requests[this.index % this.requests.length]
    return new Promise((resolve, rej) => {
      axios
        .get(request.url) // TODO: non GET's
        .then(res => {
          console.log(res)
          resolve(res)
        })
        .catch(err => {
          console.log(err)
          resolve({
            err,
            data: null,
          })
        })
    })
      .then(res => {
        const duration = new Date().getTime() - startTime
        return new Promise(resolve =>
          resolve({
            runner: this.id,
            startTime,
            duration,
            data: res.data,
            request,
          }),
        )
      })
      .then(exe => {
        this.results.push(exe)
        if (this.index++ < this.count && this.running) {
          this.promise = this._request()
          return this.promise
        }
        this.running = false
        return new Promise(resolve => resolve(this.results))
      })
  }

  start() {
    if (!this.running) {
      this.running = true
      this.index = 1
      this.promise = this._request()
    }
    return this.promise
  }

  stop() {
    this.running = false
  }
}

class PerfRunner {
  constructor(runConfig) {
    this.id = uuid()
    this.runConfig = runConfig
    this.requests = runConfig.exeDurr.map(({ value }) => ({
      url: `http://localhost:8080/rl/fib/calc/${value}`,
    }))
    this.running = false
    this.isDone = false
    this.runnerProcessCount = Math.ceil(runConfig.count / runConfig.concurrency)
    this.expectedCount = this.runnerProcessCount * runConfig.concurrency
    this.runners = _.range(runConfig.concurrency).map(
      i => new Runner(this.runnerProcessCount, this.requests, i),
    )
  }

  isRunning() {
    return this.running
  }

  start() {
    if (this.running) {
      return
    }
    this.running = true
    const startTime = new Date().getTime()
    return Promise.all(this.runners.map(r => r.start())).then(runnerResults => {
      const mappedResults = {}
      let totalReqTime = 0
      let totalReqs = 0
      let minReqDurr = Number.MAX_SAFE_INTEGER
      let maxReqDurr = Number.MIN_SAFE_INTEGER
      runnerResults.forEach(results => {
        let runnerId = 0
        totalReqs += results.length
        results.forEach(res => {
          runnerId = res.runner
          totalReqTime += res.duration
          minReqDurr = Math.min(minReqDurr, res.duration)
          maxReqDurr = Math.max(maxReqDurr, res.duration)
        })
        mappedResults[runnerId] = results
      })
      const summary = {
        totalReqTime,
        totalReqs,
        minReqDurr,
        maxReqDurr,
        meanDur: totalReqTime / totalReqs,
      }

      return new Promise(resolve => {
        this.running = false
        resolve({
          startTime,
          runId: uuid(),
          runnerId: this.id,
          results: mappedResults,
          summary,
        })
      })
    })
  }

  getProgress() {
    const complete = _.sumBy(this.runners, 'index') - this.runConfig.concurrency
    return {
      complete,
      total: this.expectedCount,
      percentage: complete / this.expectedCount,
      percentageInt: Math.floor(complete / this.expectedCount * 100),
    }
  }

  stop() {
    this.runners.forEach(r => r.stop())
  }
}

// const testConfig = {
//   exeDurr: [3, 16].map(value => ({ value })),
//   count: 10,
//   concurrency: 2,
// }
// new PerfRunner(testConfig)
//   .start()
//   .then(res => console.log(JSON.stringify(res.summary, null, 2)))

export default PerfRunner
