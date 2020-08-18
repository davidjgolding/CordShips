import React from "react"
import GameBoard from "../GameBoard/GameBoard";
import "./EnemyBoards.scss"

function EnemyBoards(props){

    let enemyBoards = []

    for(let i = 0; i < props.enemyStates.length; i++){
        enemyBoards.push(<div className="enemyBoardWrapper"> <div className="enemyTitle">Enemy Name </div><GameBoard gameBoardState={props.enemyStates[i]}></GameBoard> </div>)
    }

    return(
        <div className="enemyBoardsContainer">
            {enemyBoards}
        </div>
    )
}

export default EnemyBoards;