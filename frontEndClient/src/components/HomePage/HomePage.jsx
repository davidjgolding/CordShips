import React from "react"
import { BoardEnum } from "../../App"
import GameBoard from "../GameBoard/GameBoard"
import "./HomePage.scss"
import logo from '../../16-10-31_R3_Corda_Master Logo-03.jpg';
import EnemyBoards from "../EnemyBoards/EnemyBoards";


//The homepage function is the wrapped for the whole front page
function HomePage(props){

    //mockup some enemy state data
    let enemyStates = []

    for (let i = 0; i < 6; i++) {
        let enemyState = [];
        for (let y = 0; y < 10; y++) {
            let row = []
            for (let x = 0; x < 10; x++) {
                row.push({ x: x, y: y, type: BoardEnum.Hit })
            }
            enemyState.push(row)
        }
        enemyStates.push(enemyState)
    }

    //Mock up some player state data

    let playerState = []

    for(let y = 0; y < 10; y ++){
      let row = []
      for(let x = 0; x < 10; x++){
        row.push({x:x, y:y, type: BoardEnum.Ship})
      }
      playerState.push(row)
    }

    return(
        <div className="homeWrapper">
            <div className="navHeader">
                <img className="cordaLogo" src={logo}></img>
                 <div className="appTitle">You are playing CordShips! </div>
            </div>
            
            <div className="playerGameStateTitle"> 
                <div>Your Battle State</div>
                <div className="indicatorWrapper">
                    <div className="colorKey aliveKey">Alive</div>
                    <div className="colorKey hitKey">Hit</div>
                </div>
            </div>
            
            <div className="playerBoardWrapper">
                <GameBoard gameBoardState={playerState}></GameBoard>
            </div>

            <div className="enemySecionTitle">Enemy Battle States</div>

            <div className="enemyIndicatorWrapper">
                <div className="colorKey missedKey">Missed</div>
                <div className="colorKey selectedKey">Selected</div>
                <div className="colorKey hitKey">Hit</div>
            </div>

            <div className="enemySectionWrapper">
                <EnemyBoards enemyStates={enemyStates}></EnemyBoards>
            </div>
        </div>
    )
}

export default HomePage;