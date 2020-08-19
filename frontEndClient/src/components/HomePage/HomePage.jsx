import React, {useState} from "react"
import { BoardEnum } from "../../boardEnum/boardEnum"
import GameBoard from "../GameBoard/GameBoard"
import "./HomePage.scss"
import logo from '../../16-10-31_R3_Corda_Master Logo-03.jpg';
import EnemyBoards from "../EnemyBoards/EnemyBoards";
import { useInterval } from "../../clientapi/pollhook";
import axios from 'axios';
import { USER_HOST, USER_ID, POLLING_INTERVAL } from "../../App";


//The homepage function is the wrapped for the whole front page
function HomePage(props){

    const [playerState, setPlayerState] = useState([])
    const [enemyStates, setEnemyStates] = useState([])
    const [userName, setUserName] = useState("Not Logged In")
    const [userTurn, setUserTurn] = useState("Defualt")
    const [winner, setWinner] = useState("Game Still In Progress")
    const [gameProgressClass, setGameProgressClass] = useState("inProgress")

    //This needs to be set to false once we know its the players turn
    const [isPolling, setIsPolling] = useState(true)

    const stopPolling = () => {
        setIsPolling(false)
    }

    const processAndSetEnemyStates = (enemyData) => {
        let tempEnemyStates = []

        for(let key in enemyData){
            let enemyState = [];
            for (let y = 0; y < 10; y++) {
                let row = []
                for (let x = 0; x < 10; x++) {
                    row.push({ x: x, y: y, type: enemyData[key][x][y], id: key})
                }
                enemyState.push(row)
            }
            tempEnemyStates.push({id: key,enemyState:enemyState})
        }

        setEnemyStates(tempEnemyStates);
    }

    const processAndSetPlayerState = (playerGrid) => {
        let playerState = []

        for(let y = 0; y < 10; y++){
            let row = playerGrid[y]
            let newStateRow = []
            for(let x = 0; x < 10; x++){
                newStateRow.push({x:y,y:y,type:playerGrid[x][y]})
            }
            playerState.push(newStateRow)
        }

        setPlayerState(playerState)
    }

    //This is used for polling the server
    useInterval(async() => {
        console.log("polling")

        axios.get(`${USER_HOST}api/grids`, {
            params: {
                playerID: USER_ID
            }
        }).then(function (response) {
                processAndSetPlayerState(response.data.grid)
        }).catch(function (error) {
                console.log(error);
        });

        axios.get(`${USER_HOST}api/competitorsGrids`).then(function (response) {
            console.log(response.data)
                processAndSetEnemyStates(response.data)
        }).catch(function (error) {
                console.log(error);
        });

        axios.get(`${USER_HOST}api/id`).then(function (response) {
            setUserName(response.data.playerID)
        }).catch(function (error) {
                console.log(error);
        });

        axios.get(`${USER_HOST}api/turn`).then(function (response) {
            if(response.data.winner.includes("Party")){
                if(response.data.winner == USER_ID){
                    setWinner("You have won the game!")
                }else{
                    setWinner(response.data.winner + " has won the game")
                }
                
                setGameProgressClass("winner");
            }

            setUserTurn(response.data.turn)
        }).catch(function (error) {
                console.log(error);
        });

    }, isPolling ? POLLING_INTERVAL : null);

    return(
        <div className="homeWrapper">
            <div className="navHeader">
                <img className="cordaLogo" src={logo}></img>
                 <div className="appTitle" data-end=".">{userName}  You are playing CordShips</div>
            </div>
            
            <div className="playerGameStateTitle"> 
                <div className={gameProgressClass}>{winner}</div>
                <div>Your Battle State</div>
                <div>Current Users Turn : {userTurn}</div>
                <div className="indicatorWrapper">
                    <div className="colorKey aliveKey">Alive</div>
                    <div className="colorKey hitKey">Hit</div>
                </div>
            </div>
            
            <div className="playerBoardWrapper">
                <GameBoard gameBoardState={playerState}></GameBoard>
            </div>

            <button onClick={stopPolling}> Stop Polling</button>

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