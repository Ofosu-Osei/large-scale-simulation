import { renderHook, act } from '@testing-library/react-hooks';
import { Server } from 'jest-websocket-mock';
import useWebSocket from './useWebSocket';

const WS_URL = 'http://localhost:8080/ws-command';

let server;
beforeEach(() => {
  server = new Server(WS_URL);
});
afterEach(() => {
  server.close();
});

test('connects and sends messages', async () => {
  const { result, waitForNextUpdate } = renderHook(() => useWebSocket());
  // Wait for connection
  await waitForNextUpdate();
  expect(result.current.connected).toBe(true);

  act(() => {
    result.current.sendCommand({ command: "test command" });
  });
  // Wait for server to receive message
  await expect(server).toReceiveMessage(JSON.stringify({ command: "test command" }));
  
  // Simulate a server update
  server.send(JSON.stringify({ update: "Server Test Update" }));
  // Assert that updates array has new update
  await waitForNextUpdate();
  expect(result.current.updates).toContainEqual({ update: "Server Test Update" });
});
