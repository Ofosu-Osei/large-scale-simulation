import React from 'react';
import { Box, Typography } from '@mui/material';

function LogPanel({ messages }) {
  return (
    <Box
      sx={{
        height: '120px',
        overflowY: 'auto',
        borderTop: '1px solid #ccc',
        padding: '0.5rem',
      }}
    >
      <Typography variant="subtitle1">Log Output:</Typography>
      {messages.map((msg, index) => (
        <div key={index} style={{ fontSize: '0.9rem' }}>
          {msg}
        </div>
      ))}
    </Box>
  );
}

export default LogPanel;
