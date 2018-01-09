import _ from 'lodash'

export default class {
  constructor(opts) {
    _.assignIn(this, opts)
  }
}
