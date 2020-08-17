import React from 'react';
import logo from './logo.svg';
import './App.css';
import GameBoard from './components/GameBoard/GameBoard';
import HomePage from './components/HomePage/HomePage';

export const BoardEnum = Object.freeze({"Empty":1, "Ship":2, "Miss":3, "Hit":4})

function App() {

  return (
    <div>
      <HomePage></HomePage>
    </div>
  )
}

export default App;
