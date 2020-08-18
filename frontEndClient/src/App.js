import React from 'react';
import logo from './logo.svg';
import './App.css';
import GameBoard from './components/GameBoard/GameBoard';
import HomePage from './components/HomePage/HomePage';
import { AppProvider } from './context/AppContext';

export const SHOT_LIMIT = 3;
export const POLLING_INTERVAL = 100 * 20;
export const USER_HOST = "http://localhost:10050/";
export const USER_ID = "playerA";

function App() {

  return (
    <AppProvider>
      <div>
        <HomePage></HomePage>
      </div>
    </AppProvider>
  )
}

export default App;
