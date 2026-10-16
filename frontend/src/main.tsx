import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import './index.css'
import Layout from './components/Layout.tsx'
import ConcertListPage from './pages/ConcertListPage.tsx'
import SeatMapPage from './pages/SeatMapPage.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route index element={<ConcertListPage />} />
          <Route path="concerts/:concertId" element={<SeatMapPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  </StrictMode>,
)
