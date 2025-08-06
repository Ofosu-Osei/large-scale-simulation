import { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export default function useWebSocket(onUpdate) {
  const [connected, setConnected] = useState(false);
  const [updates, setUpdates] = useState([]);   
  const clientRef = useRef(null);

  useEffect(() => {
    //192.168.1.254
    const socket = new SockJS('http://localhost:8081/api/ws-command');
    const client = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        setConnected(true);
        console.log('Connected to Simulation Server');

        // Subscribe to the main command result topic
        client.subscribe('/topic/command-result', (message) => {
          const update = JSON.parse(message.body);
          console.log('Received command result:', update);
          setUpdates((prev) => [...prev, update]);
          if (onUpdate) {
            onUpdate(update);
          }
        });

         // Subscribe to load command result topic
         client.subscribe('/topic/loadCommand-result', (message) => {
          const update = JSON.parse(message.body);
          console.log('Received loadCommand result:', update);
          setUpdates((prev) => [...prev, update]);
          if (onUpdate) {
            onUpdate(update);
          }
        });

        // Subscribe to new building result topic
        client.subscribe('/topic/newBuilding-result', (message) => {
          const update = JSON.parse(message.body);
          console.log('Received newBuilding result:', update);
          setUpdates((prev) => [...prev, update]);
          if (onUpdate) {
            onUpdate(update);
          }
        });
        // Subscribe to session‐load result topic
        client.subscribe('/topic/loadSession-result', msg => {
          const sessionState = JSON.parse(msg.body);
          onSessionLoad?.(sessionState);
        });

       
      },
      onDisconnect: () => {
        setConnected(false);
        console.log('Disconnected from Simulation Server');
      },
      onStompError: (frame) => {
        console.error('Broker error:', frame.headers['message']);
        console.error('Error details:', frame.body);
      },
    });
    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, []);

  function safePublish(destination, body) {
    if (!connected || !clientRef.current) {
      console.warn(`Can't publish to ${destination}, socket not connected yet.`);
      return false;
    }
    clientRef.current.publish({ destination, body });
    return true;
  }

  const sendCommand = (id, cmdObj) =>
    safePublish('/app/command', JSON.stringify(id, cmdObj));

  const sendNewBuildingCommand = (id, cmdObj) =>
    safePublish('/app/newBuilding', JSON.stringify(id, cmdObj));

  const sendLoadCommand = (id, cmdObj) =>
    safePublish('/app/loadCommand', JSON.stringify(id, cmdObj));

  const sendLoadSession = sessionId =>
  safePublish('/app/loadSession', sessionId.toString());

  return { connected, sendCommand, sendNewBuildingCommand, sendLoadCommand, sendLoadSession, updates };
}
