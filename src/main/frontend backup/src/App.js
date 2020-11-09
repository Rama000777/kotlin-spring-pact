import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';
class App extends Component {
   state = {
      isLoading: false,
      greeting: ""
   };
   sayHello = async (event) => {
      event.preventDefault();
      let response = await fetch('/greeting?name=' + this.state.greeting);
      let body = await response.json();
      this.setState({ greeting: body.name, isLoading: false, isGreetingVisible: '' });
   }
   updateName = (event) => {
      event.preventDefault();
      this.setState({ greeting: event.target.value, isLoading: false });
   }
   render() {
      const { greeting, isLoading } = this.state;
      if (isLoading) {
         return
      }
      return (
         <div className="App">
            <header className="App-header">
               <img src={logo} className="App-logo" alt="logo" />
               <div className="App-intro">
                  <input onChange={(event) => this.updateName(event)} placeholder="Enter Your Name"></input>
                  <button onClick={(event) => this.sayHello(event)}>Please Click Me!</button>
                  <h2 style={{ visibility: this.isGreetingVisible }}>Hello {this.state.greeting}</h2>
               </div>
            </header>
         </div>
      );
   }
}
export default App;