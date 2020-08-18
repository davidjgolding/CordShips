import React, { Component } from "react"

const AppContext = React.createContext()

export const AppConsumer = AppContext.Consumer

class AppProvider extends Component {
    state = { selected: [] }

    setAppState = (selected) => {
        this.setState((prevState) => ({ selected }))
    }

    render() {
        const { children } = this.props
        const { selected } = this.state
        const { setAppState } = this

        return (
            <AppContext.Provider
                value={{
                    selected,
                    setAppState,
                }}
            >
                {children}
            </AppContext.Provider>
        )
    }
}

export default AppContext

export {AppProvider}
