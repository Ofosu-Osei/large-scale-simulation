import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import SessionModal from './components/SessionModal';
import Snackbar from '@mui/material/Snackbar';
import useWebSocket from './hooks/useWebSocket';
import TopMenu from './components/TopMenu';
import BuildingsPanel from './components/BuildingsPanel';
import SimulationCanvas from './components/SimulationCanvas';
import RightControls from './components/RightControls';
import NewBuildingModal from './components/NewBuildingModal';
import LoadModal from './components/LoadModal';
import SaveModal from './components/SaveModal';
import './App.css';

function App() {
  const { sessionId } = useParams();
  const navigate = useNavigate();

  // 最开始判断：如果没有 sessionId，弹出 Modal
  const [isSessionModalOpen, setIsSessionModalOpen] = useState(!sessionId);
  const [sessionError, setSessionError] = useState('');
  const [loadingSession, setLoadingSession] = useState(false);
  const lastSessionIdRef = useRef(Number(sessionId || 0));

  const [buildings, setBuildings] = useState([]);
  const [roads, setRoads] = useState([]);
  const [cycle, setCycle] = useState(0);

  const [sourcePolicy, setSourcePolicy] = useState('qlen');
  const [requestPolicy, setRequestPolicy] = useState('fifo');
  const [requestPolicyOn, setRequestPolicyOn] = useState('*');
  const [verbosity, setVerbosity] = useState(1);

  const [isNewBuildingOpen, setIsNewBuildingOpen] = useState(false);
  const [isLoadModalOpen, setIsLoadModalOpen] = useState(false);
  const [isSaveModalOpen, setIsSaveModalOpen] = useState(false);
  const [pendingSaveFilename, setPendingSaveFilename] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  // ⚡ 先定义这两个函数
  const handleSimulationUpdate = (update) => {
    const currentSessionId = Number(sessionId);

    if (update.id !== undefined && update.id !== currentSessionId) {
      return; // id不匹配，不更新
    }

    const data = update.jsonData || {};

    if (data.status === 'error') {
      setErrorMessage(data.message || data.details || 'An error occurred');
    } else {
      if (pendingSaveFilename) {
        downloadSimulationJSON(data, pendingSaveFilename);
        setPendingSaveFilename('');
      }
      if (typeof data.cycle === 'number') setCycle(data.cycle);
      if (data.buildings) setBuildings(data.buildings);
      if (data.roads) setRoads(data.roads);
    }
  };

  const handleSessionLoad = (sessionState) => {
    setIsSessionModalOpen(false);
    if (sessionState.status === 'error') {
      setErrorMessage(sessionState.message);
      setLoadingSession(false);
    } else {
      const data = sessionState.jsonData || {};
      setBuildings(data.buildings || []);
      setRoads(data.roads || []);
      setCycle(data.cycle || 0);
      localStorage.setItem('sessionId', lastSessionIdRef.current);
    }
  };

  // 🔥 再 useWebSocket
  const {
    connected,
    sendCommand,
    sendNewBuildingCommand,
    sendLoadCommand,
    sendLoadSession,
    updates,
  } = useWebSocket(handleSimulationUpdate, handleSessionLoad);

  useEffect(() => {
    if (updates.length > 0) {
      const latest = updates[updates.length - 1];
      const data = latest.jsonData || {};
      if (data.status === 'error') {
        setErrorMessage(data.message || data.details || 'An error occurred');
      } else {
        if (pendingSaveFilename) {
          downloadSimulationJSON(data, pendingSaveFilename);
          setPendingSaveFilename('');
        }
      }
    }
  }, [updates, pendingSaveFilename]);

  // 封装 send 方法
  const wrappedSendCommand = (cmdObj) =>
    sendCommand({ id: lastSessionIdRef.current, ...cmdObj });
  const wrappedSendNewBuilding = (jsonData) =>
    sendNewBuildingCommand({ id: lastSessionIdRef.current, jsonData });
  const wrappedSendLoad = (jsonData) =>
    sendLoadCommand({ id: lastSessionIdRef.current, jsonData });
  const wrappedLoadSession = (id) => {
    lastSessionIdRef.current = id;
    sendLoadSession(id);
  };

  // 用户输入 id 后，加载 session 并跳转
  const onLoadSession = (id) => {
    setLoadingSession(true);
    setSessionError('');
    lastSessionIdRef.current = id;
    setIsSessionModalOpen(false);
    navigate(`/${id}`);
    wrappedLoadSession(id);
  };

  const handleOpenSaveModal = () => setIsSaveModalOpen(true);
  const handleCloseSaveModal = () => setIsSaveModalOpen(false);
  const handleSaveData = (filename) => {
    setPendingSaveFilename(filename);
    wrappedSendCommand({ command: `save ${filename}` });
  };
  const handleOpenLoadModal = () => setIsLoadModalOpen(true);
  const handleCloseLoadModal = () => setIsLoadModalOpen(false);
  const handleNewBuildingClick = () => setIsNewBuildingOpen(true);
  const handleCloseNewBuilding = () => setIsNewBuildingOpen(false);
  const handleCreateBuilding = (data) => {
    wrappedSendNewBuilding(data);
    setIsNewBuildingOpen(false);
  };

  const downloadSimulationJSON = (jsonData, filename) => {
    const blob = new Blob([JSON.stringify(jsonData, null, 2)], {
      type: 'application/json',
    });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = filename;
    link.click();
  };

  const handleCloseSnackbar = (e, reason) => {
    if (reason === 'clickaway') return;
    setErrorMessage('');
  };

  // 🚀 一开始如果没有sessionId，弹出 Modal
  if (isSessionModalOpen) {
    return (
      <SessionModal
        open={isSessionModalOpen}
        loading={loadingSession}
        error={sessionError}
        onLoadSession={onLoadSession}
        onClose={() => setIsSessionModalOpen(false)}
      />
    );
  }

  return (
    <div className="app-container">
      <TopMenu
        onSaveModalOpen={handleOpenSaveModal}
        onLoad={handleOpenLoadModal}
        onExit={() => {}}
        connected={connected}
      />

      <div className="main-layout">
        <div className="left-panel">
          <BuildingsPanel buildings={buildings} />
        </div>

        <div className="center-panel">
          <SimulationCanvas
            buildings={buildings}
            roads={roads}
            sendCommand={wrappedSendCommand}
          />
        </div>

        <div className="right-panel">
          <RightControls
            connected={connected}
            cycle={cycle}
            buildings={buildings}
            onNewBuilding={handleNewBuildingClick}
            onStep={(n) => wrappedSendCommand({ command: `step ${n}` })}
            onFinish={() => wrappedSendCommand({ command: 'finish' })}
            sourcePolicy={sourcePolicy}
            requestPolicy={requestPolicy}
            requestPolicyOn={requestPolicyOn}
            onSourcePolicyChange={setSourcePolicy}
            onRequestPolicyChange={setRequestPolicy}
            onRequestPolicyOnChange={setRequestPolicyOn}
            verbosity={verbosity}
            onVerbosityChange={setVerbosity}
            sendCommand={wrappedSendCommand}
          />
        </div>
      </div>

      <NewBuildingModal
        open={isNewBuildingOpen}
        onClose={handleCloseNewBuilding}
        onCreate={handleCreateBuilding}
      />
      <LoadModal
        open={isLoadModalOpen}
        onClose={handleCloseLoadModal}
        onLoad={wrappedSendLoad}
      />
      <SaveModal
        open={isSaveModalOpen}
        onClose={handleCloseSaveModal}
        onSave={handleSaveData}
      />

      <Snackbar
        open={Boolean(errorMessage)}
        autoHideDuration={6000}
        onClose={handleCloseSnackbar}
        message={errorMessage}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      />
    </div>
  );
}

export default App;

