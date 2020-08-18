import React from "react"
import "./GameBoard.scss"
import { BoardEnum } from "../../boardEnum/boardEnum"

function GameBoard(props){

    let coordinates = props.gameBoardState
    let elements = []

    //quick fix to avoid rendering errors
    if (coordinates.length > 0) {

        for (let y = 0; y < 10; y++) {
            let row = coordinates[y]
            let renderedRow = []

            row.forEach(element => {
                let idString = element.x + "," + element.y

                let gridItemType = "gameBoardItem "
                
                //0=Empty, 1=Hidden Square, 2=Ship, 3=Miss, 4=Hit

                switch (element.type) {
                    case 0:
                        gridItemType += "emptyBoardItem"
                        break;
                    case 4:
                        gridItemType += "hitBoardItem"
                        break;
                    case 3:
                        gridItemType += "missBoardItem"
                        break;
                    case 2:
                        gridItemType += "shipBoardItem"
                        break;
                }

                renderedRow.push(<td id={idString} className={gridItemType}></td>)
            });

            elements.push(<tr className="gameBoardRow">{renderedRow}</tr>)
        }
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