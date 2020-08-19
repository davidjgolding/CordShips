import React from 'react';
import logo from './logo.svg';
import './App.css';
import GameBoard from './components/GameBoard/GameBoard';
import HomePage from './components/HomePage/HomePage';
import { AppProvider } from './context/AppContext';

export const SHOT_LIMIT = 3;
export const POLLING_INTERVAL = 100 * 20;
export const USER_HOST = process.env.REACT_APP_APIHOST.trim();
export const USER_ID = process.env.REACT_APP_USER_ID.trim();

function App() {

  console.log("USERHOST" + USER_HOST)
  console.log("USEID" + USER_ID)

  return (
    <AppProvider>
      <div>
        <HomePage></HomePage>
      </div>
    </AppProvider>
  )
}

export default App;
