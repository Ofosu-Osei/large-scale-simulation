import React, { useState } from 'react';
import {
  Dialog, DialogTitle, DialogContent,
  DialogActions, Button, TextField, Typography
} from '@mui/material';

export default function SessionModal({
  open, onClose, onLoadSession, loading, error
}) {
  const [sessionId, setSessionId] = useState('');

  const handleLoad = () => {
    onLoadSession(Number(sessionId));
  };

  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Enter Session PIN</DialogTitle>
      <DialogContent>
        <Typography gutterBottom>
          Type the numeric PIN for your session.
        </Typography>
        <TextField
          label="Session ID"
          type="password"
          value={sessionId}
          onChange={e => {
            const digits = e.target.value.replace(/\D/g, '');
            setSessionId(digits);
          }}
          inputProps={{
            maxLength: 6,
            inputMode: 'numeric',
            pattern: '[0-9]*',
          }}
          fullWidth
          size="small"
          margin="dense"
        />
        {error && (
          <Typography color="error" variant="body2">
            {error}
          </Typography>
        )}
      </DialogContent>
      <DialogActions>
        <Button
          onClick={handleLoad}
          disabled={loading || sessionId.length === 0}
        >
          Load Session
        </Button>
      </DialogActions>
    </Dialog>
  );
}
