import Office from './components/office'
import LibraryLogin from './components/LibraryLogin'
import LibraryMain from './components/LibraryMain'
import { Navigate, Route, Routes } from 'react-router-dom'

function App() {
  return (
    <div>
      <Routes>
        <Route path="/" element={<Navigate to="/office" replace />} />
        <Route path="/office" element={<Office />} />
        <Route path="/library" element={<LibraryLogin />} />
        <Route path="/main" element={<LibraryMain />} />
      </Routes>
    </div>
  )
}

export default App
