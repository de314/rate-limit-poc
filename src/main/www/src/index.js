import React from 'react'
import ReactDOM from 'react-dom'
import App from './App'
import registerServiceWorker from './registerServiceWorker'

import PerfRunner from './PerfRunner'

import 'react-select/dist/react-select.css'

ReactDOM.render(<App />, document.getElementById('root'))
registerServiceWorker()
