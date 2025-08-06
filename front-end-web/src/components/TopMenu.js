import React from 'react';
import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material';

function TopMenu({ onSaveModalOpen, onLoad, onExit, connected }) {
  return (
    <AppBar position="static">
      <Toolbar>
        <Typography variant="h6" style={{ flexGrow: 1 }}>
          Simulation GUI
        </Typography>
        <Button color="inherit" onClick={onSaveModalOpen} disabled={!connected}>
          Save
        </Button>
        <Button color="inherit" onClick={onLoad} disabled={!connected}>
          Load
        </Button>
        <Button color="inherit" onClick={onExit} disabled={!connected}>
          Exit
        </Button>

         {/* Connection Status indicator */}
         <Box
          sx={{
            mr: 2,
            px: 1,
            py: 0.5,
            borderRadius: 1,
            backgroundColor: connected ? 'lightgreen' : 'lightcoral',
            color: 'black',
            fontSize: '0.9rem',
          }}
        >
          {connected ? 'Connected' : 'Disconnected'}
        </Box>
      </Toolbar>
    </AppBar>
  );
}

export default TopMenu;
