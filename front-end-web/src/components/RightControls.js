import React, { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Button,
  TextField,
  Select,
  MenuItem,
  InputLabel,
  FormControl,
  Typography,
  Switch,
  FormControlLabel,
} from '@mui/material';

function RightControls({
  connected,
  cycle,
  buildings = [], 
  onNewBuilding,
  sendCommand, 
  sourcePolicy,
  requestPolicy,
  requestPolicyOn,
  onSourcePolicyChange,
  onRequestPolicyChange,
  onRequestPolicyOnChange,
  verbosity,
  onVerbosityChange,
}) {
  // Local states for command input values
  const [stepCount, setStepCount] = useState(1);
  const [localVerbose, setLocalVerbose] = useState(verbosity);
  const [connectSource, setConnectSource] = useState('');
  const [connectDest, setConnectDest] = useState('');
  const [requestItemName, setRequestItemName] = useState('');
  const [requestBuildingName, setRequestBuildingName] = useState('');
  const [autoStep, setAutoStep] = useState(false);

  // Local state for policy dropdowns
  const [localSourcePolicy, setLocalSourcePolicy] = useState(sourcePolicy);
  const [localRequestPolicy, setLocalRequestPolicy] = useState(requestPolicy);

  // Handlers that build commands in the required format
  const doStep = useCallback(() => {
    if (!connected) return;
    const cmd = `step ${stepCount}`;
    sendCommand({ command: cmd });
  }, [connected, sendCommand, stepCount]);

  // If autoStep flips ON, start an interval and clear it when OFF
  useEffect(() => {
    if (autoStep) {
      const id = setInterval(doStep, 1000);
      return () => clearInterval(id);
    }
  }, [autoStep, doStep]);
  
  // STEP command: "step 1"
  const handleStepCommand = () => {
    if (!connected) {
      alert("Still connecting to the server…");
      return;
    }
    const commandString = `step ${stepCount}`;
    sendCommand({ command: commandString });
  };

  // VERBOSE command: "verbose 1"
  const handleVerboseCommand = () => {
    if (!connected) return;
    const commandString = `verbose ${localVerbose}`;
    sendCommand({ command: commandString });
    onVerbosityChange(Number(localVerbose));
  };

  // CONNECT command: "connect 'SOURCE_NAME' to 'DEST_NAME'"
  const handleConnectCommand = () => {
    if (!connected) return;
    const commandString = `connect '${connectSource}' to '${connectDest}'`;
    sendCommand({ command: commandString });
  };

  // DISCONNECT command: "disconnect 'SOURCE_NAME' to 'DEST_NAME'"
  const handleDisconnectCommand = () => {
      if (!connected) return;
      const commandString = `disconnect '${connectSource}' to '${connectDest}'`;
      sendCommand({ command: commandString });
    };

  // REQUEST command: "request 'ITEM' from 'BUILDING'"
  const handleRequestCommand = () => {
    if (!connected) return;
    const commandString = `request '${requestItemName}' from '${requestBuildingName}'`;
    sendCommand({ command: commandString });
  };

  // FINISH command: "finish"
  const handleFinishCommand = () => {
    if (!connected) return;
    sendCommand({ command: "finish" });
    console.log("Sending finish command");
  };

  // SET POLICY commands
  const handleSetSourcePolicy = () => {
    if (!connected) return;
    const quotedPolicy = `'${localSourcePolicy}'`;
    const target =
      requestPolicyOn === '*'
        ? '*'
        : `'${requestPolicyOn}'`;
    const commandString = `set policy source ${quotedPolicy} on ${target}`;
    console.log('Sending:', commandString);
    sendCommand({ command: commandString });
    onSourcePolicyChange(localSourcePolicy);
  };

  const handleSetRequestPolicy = () => {
    if (!connected) return;
    const quotedPolicy = `'${localRequestPolicy}'`;
    const target =
      requestPolicyOn === '*'
        ? '*'
        : `'${requestPolicyOn}'`;
    const commandString = `set policy request ${quotedPolicy} on ${target}`;
    console.log('Sending:', commandString);
    sendCommand({ command: commandString });
    onRequestPolicyChange(localRequestPolicy);
  };


  return (
    <Box sx={{ width: '250px', padding: '1rem', borderLeft: '1px solid #ccc' }}>
      <Box display="flex" alignItems="center" gap={1} mb={2}>
        <Button variant="contained" color="primary" onClick={onNewBuilding} disabled={!connected}>
          New Building
        </Button>
        <Typography variant="subtitle1" sx={{ fontSize: '1.2rem', fontWeight: 'bold' }}>
          Cycle: {cycle}
        </Typography>
      </Box>

      <Box mt={2}>
        <Typography variant="subtitle1">Simulation Controls:</Typography>
        {/* Step Control */}
        <Box display="flex" alignItems="center" gap={1}>
          <TextField
            type="number"
            label="Step Cycles"
            value={stepCount}
            onChange={(e) => setStepCount(e.target.value)}
            size="small"
            disabled={autoStep}
            sx={{ width: 120 }}
          />
          
        {/* Auto‑Step Switch */}
        <FormControlLabel
            control={
              <Switch
                checked={autoStep}
                onChange={(e) => setAutoStep(e.target.checked)}
                disabled={!connected}
              />
            }
            label="Auto"
            labelPlacement="start"
          />
        </Box>
        {/* Finish Command */}
        <Box display="flex" gap={3} mt={1}>
          <Button variant="outlined" color="secondary" onClick={handleFinishCommand} disabled={!connected}>
            Finish
          </Button>
          <Button variant="outlined" onClick={handleStepCommand} disabled={!connected || autoStep}>
            Step
          </Button>
        </Box>
      </Box>

      <Box mt={2}>
        <Typography variant="subtitle1">Set Policies:</Typography>
        {/* Source Policy with Set button */}
        <Box display="flex" alignItems="center" gap={1}>
          <FormControl fullWidth margin="dense">
            <InputLabel>Source Policy</InputLabel>
            <Select
              value={localSourcePolicy}
              label="Source Policy"
              onChange={(e) => setLocalSourcePolicy(e.target.value)}
              size="small"
            >
              <MenuItem value="qlen">qlen</MenuItem>
              <MenuItem value="simplelat">simplelat</MenuItem>
              <MenuItem value="recursivelat">recursivelat</MenuItem>
            </Select>
          </FormControl>
          <Button variant="outlined" size="small" onClick={handleSetSourcePolicy} disabled={!connected}>
            Set
          </Button>
        </Box>
        {/* Request Policy with Set button */}
        <Box display="flex" alignItems="center" gap={1}>
          <FormControl fullWidth margin="dense">
            <InputLabel>Request Policy</InputLabel>
            <Select
              value={localRequestPolicy}
              label="Request Policy"
              onChange={(e) => setLocalRequestPolicy(e.target.value)}
              size="small"
            >
              <MenuItem value="fifo">fifo</MenuItem>
              <MenuItem value="ready">ready</MenuItem>
              <MenuItem value="sjf">sjf</MenuItem>
            </Select>
          </FormControl>
          <Button variant="outlined" size="small" onClick={handleSetRequestPolicy} disabled={!connected}>
            Set
          </Button>
        </Box>
        {/* Building selection dropdown for policies */}
        <FormControl fullWidth margin="dense">
          <InputLabel>Building(s)</InputLabel>
          <Select
            value={requestPolicyOn}
            label="Building(s)"
            onChange={(e) => onRequestPolicyOnChange(e.target.value)}
            size="small"
          >
            <MenuItem value="*">All Buildings</MenuItem>
            {buildings.map((b) => (
              <MenuItem key={b.name} value={b.name}>
                {b.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>

      {/* Verbosity Control */}
      <Box mt={2}>
        <Typography variant="subtitle1">Set Verbosity Level:</Typography>
        <Box display="flex" alignItems="center" gap={1}>
          <TextField
            type="number"
            size="small"
            value={localVerbose}
            onChange={(e) => setLocalVerbose(e.target.value)}
            fullWidth
          />
          <Button variant="outlined" size="small" onClick={handleVerboseCommand} disabled={!connected}>
            Set
          </Button>
        </Box>
      </Box>

      {/* Connect and Request Commands */}
      <Box mt={2}>
        <Typography variant="subtitle1">Connect Buildings:</Typography>
        <TextField
          label="Source"
          size="small"
          fullWidth
          margin="dense"
          value={connectSource}
          onChange={(e) => setConnectSource(e.target.value)}
        />
        <TextField
          label="Dest"
          size="small"
          fullWidth
          margin="dense"
          value={connectDest}
          onChange={(e) => setConnectDest(e.target.value)}
        />
        <Box display="flex" gap={1} mt={1}>
          <Button
            variant="outlined"
            fullWidth
            onClick={handleConnectCommand}
            disabled={!connected}
            sx={{
              borderColor: 'green',
              color: 'green',
              '&:hover': {
                borderColor: 'darkgreen',
                backgroundColor: 'rgba(0,128,0,0.1)',
              },
            }}
          >
            Connect
          </Button>
          <Button
            variant="outlined"
            fullWidth
            onClick={handleDisconnectCommand}
            disabled={!connected}
            sx={{
              borderColor: 'red',
              color: 'red',
              '&:hover': {
                borderColor: 'darkred',
                backgroundColor: 'rgba(255,0,0,0.1)',
              },
            }}
          >
            Disconnect
          </Button>
       </Box>
      </Box>

      <Box mt={2}>
        <Typography variant="subtitle1">Request Item:</Typography>
        <TextField
          label="Item"
          size="small"
          fullWidth
          margin="dense"
          value={requestItemName}
          onChange={(e) => setRequestItemName(e.target.value)}
        />
        <TextField
          label="From Building"
          size="small"
          fullWidth
          margin="dense"
          value={requestBuildingName}
          onChange={(e) => setRequestBuildingName(e.target.value)}
        />
        <Button
          variant="outlined"
          fullWidth
          onClick={handleRequestCommand}
          disabled={!connected}
        >
          Request
        </Button>
      </Box>
    </Box>
  );
}

export default RightControls;
