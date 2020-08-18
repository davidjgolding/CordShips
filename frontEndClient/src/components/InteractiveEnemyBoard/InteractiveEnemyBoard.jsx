import React, {useState} from "react"
import "./InteractiveEnemyBoard.scss"
import { BoardEnum } from "../../boardEnum/boardEnum"
import { useContext } from "react"
import AppContext from "../../context/AppContext"
import { SHOT_LIMIT } from "../../App"

function InteractiveEnemyBoard(props){

    const context = useContext(AppContext)
    const [update, setUpdate] = useState(0)
    const setSelected = (event) => {
        let idXY = event.target.id.split(",")

        let newSelectedItem = {
            id: idXY[0],
            x: idXY[1],
            y: idXY[2]
        }

        let alreadySelected = context.selected.filter(
            point => point.x === idXY[1] && point.y === idXY[2])

        let toAdd; 
        if (alreadySelected.length === 0) {
            //modify the context api here add each selected if its not greater than 3
            if(context.selected.length >= SHOT_LIMIT){
                return
            }
            toAdd = [...context.selected, newSelectedItem]
        } else {
            let newSelected = context.selected.map(obj => Object.assign({}, obj))
            let index = context.selected.indexOf(alreadySelected[0]);
            if(index !== -1) { newSelected.splice(index, 1) }
            toAdd = newSelected
            console.log(newSelected)
        }

        context.setAppState(toAdd)
        setUpdate(update => update + 1)
    }


    let coordinates = props.gameBoardState
    let elements = []

    //quick fix to avoid rendering errors
    if (coordinates.length > 0) {

        for (let y = 0; y < 10; y++) {
            let row = coordinates[y]
            let renderedRow = []

            row.forEach(element => {
                let idString = element.id + "," + element.x + "," + element.y

                let gridItemType = "gameBoardItem interactiveGridElement "

                //0=Empty, 1=Hidden Square, 2=Ship, 3=Miss, 4=Hit, 5=Selected

                //Check to see if the current grid element has been selected by the player to shoot at
                context.selected.forEach(el => {
                    if(el.id == element.id && el.x == element.x && el.y == element.y){
                        element.type = 5
                    }
                })

                switch (element.type) {
                    case 0:
                        gridItemType += "emptyBoardItem"
                        break;
                    case 2:
                        gridItemType += "shipBoardItem"
                        break; 
                    case 3:
                        gridItemType += "missBoardItem"
                        break; 
                    case 4:
                        gridItemType += "hitBoardItem"
                        break;
                    case 5:
                        gridItemType += "selectedGridElement"
                        break;
                }

                renderedRow.push(<td id={idString} className={gridItemType} onClick={setSelected.bind(this)}></td>)
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

export default InteractiveEnemyBoard;