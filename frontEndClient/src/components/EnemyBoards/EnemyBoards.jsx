import React, {useState} from "react"
import "./EnemyBoards.scss"
import InteractiveEnemyBoard from "../InteractiveEnemyBoard/InteractiveEnemyBoard";
import AppContext from "../../context/AppContext";
import { useContext } from "react";
import { SHOT_LIMIT, USER_HOST } from "../../App";
import shipFiring from '../../shipfiring.gif';
import axios from "axios";

function EnemyBoards(props){

    const context = useContext(AppContext)
    let enemyBoards = []
    const [update, setUpdate] = useState(0)
    
    //quick fix to avoid rendering errors
    if (props.enemyStates.length > 0) {
        for (let i = 0; i < props.enemyStates.length; i++) {
            enemyBoards.push(<div className="enemyBoardWrapper"> <div className="enemyTitle">{props.enemyStates[i].id} </div><InteractiveEnemyBoard gameBoardState={props.enemyStates[i].enemyState}></InteractiveEnemyBoard> </div>)
        }
    }

    function endTurn(){
        if(context.selected.length < SHOT_LIMIT){
            //TODO: Display some sort of message
            return
        }else{
            context.selected.forEach(element => {
                axios.post(`${USER_HOST}api/shoot`,{ playerID: element.id,
                    x: element.x,
                    y: element.y,
                    })
                  .then(function (response) {
                    console.log(response);
                  })
                  .catch(function (error) {
                    console.log(error);
                  });
            })

            //Clear the selected state
            context.setAppState([])
            setUpdate((update) => update +1)
        }
    }

    return(
        <div>
            <div className="enemyBoardsContainer">
                {enemyBoards}
            </div>
            <div className="endTurnButtonWrapper">
                <button className="endButton" onClick={endTurn}>Click to fire and end turn!</button>
            </div>
            <div className="modal hidden"> </div>
        </div>
    )
}

export default EnemyBoards;