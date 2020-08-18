import React from "react"
import "./GameBoard.scss"
import { BoardEnum } from "../../App"

function GameBoard(props){

    let coordinates = props.gameBoardState
    let elements = []

    for(let y = 0; y < 10; y++){
        let row = coordinates[y]
        let renderedRow = []

        row.forEach(element => {
            let idString = element.x + "," + element.y

            let gridItemType = "gameBoardItem "

            switch(element.type){
                case BoardEnum.Empty:
                    gridItemType += "emptyBoardItem"
                    break;
                case BoardEnum.Hit:
                    gridItemType += "hitBoardItem"
                    break;
                case BoardEnum.Miss:
                    gridItemType += "missBoardItem"
                    break;
                case BoardEnum.Ship:
                    gridItemType += "shipBoardItem"
                    break;
            }

            renderedRow.push(<td id={idString} className={gridItemType}></td> )
        });

        elements.push(<tr className="gameBoardRow">{renderedRow}</tr>)
    }

    return(
        <div className="gameBoardWrapper">
            <table className="gameBoardTable"> 
                <tbody>
                    {elements}
                </tbody>
            </table>
        </div>
    )
}

export default GameBoard;