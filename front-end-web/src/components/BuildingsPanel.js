import React from 'react';
import {
  List,
  ListItem,
  ListItemText,
  ListSubheader,
} from '@mui/material';

function BuildingsPanel({ buildings }) {
  const mines = buildings.filter((b) => b.mine !== undefined);
  const storage = buildings.filter((b) => b.stores !== undefined);
  const wasteDisposals = buildings.filter((b) => b.disposeAmount !== undefined);
  const dronePorts = buildings.filter((b) => Array.isArray(b.drones));
  const factories = buildings.filter(
    (b) =>
      b.mine === undefined &&
      b.stores === undefined &&
      b.disposeAmount === undefined &&
      !Array.isArray(b.drones)
  );

  const wrapSx = {
    '& .MuiListItemText-primary': {
      whiteSpace: 'normal',
      wordBreak: 'break-word',
      overflowWrap: 'break-word',
    },
  };

  return (
    <div style={{ width: 200, overflowY: 'auto' }}>
      <List subheader={<ListSubheader>Buildings</ListSubheader>}>

        {factories.length > 0 && (
          <>
            <ListSubheader>Factories</ListSubheader>
            {factories.map((f) => (
              <ListItem key={f.id || f.name} sx={{ width: '100%' }}>
                <ListItemText
                  primary={`${f.name} (${f.type})`}
                  sx={wrapSx}
                />
              </ListItem>
            ))}
          </>
        )}

        {mines.length > 0 && (
          <>
            <ListSubheader>Mines</ListSubheader>
            {mines.map((m) => (
              <ListItem key={m.id || m.name} sx={{ width: '100%' }}>
                <ListItemText
                  primary={`${m.name} (mine: ${m.mine})`}
                  sx={wrapSx}
                />
              </ListItem>
            ))}
          </>
        )}

        {storage.length > 0 && (
          <>
            <ListSubheader>Storage</ListSubheader>
            {storage.map((s) => (
              <ListItem key={s.id || s.name} sx={{ width: '100%' }}>
                <ListItemText
                  primary={`${s.name} (stores: ${s.stores})`}
                  sx={wrapSx}
                />
              </ListItem>
            ))}
          </>
        )}

        {wasteDisposals.length > 0 && (
          <>
            <ListSubheader>Waste Disposal</ListSubheader>
            {wasteDisposals.map((w) => (
              <ListItem key={w.id || w.name} sx={{ width: '100%' }}>
                <ListItemText
                  primary={`${w.name} (disposeAmt: ${w.disposeAmount}, cap: ${w.capacity}, interval: ${w.disposeInterval})`}
                  sx={wrapSx}
                />
              </ListItem>
            ))}
          </>
        )}

        {dronePorts.length > 0 && (
          <>
            <ListSubheader>Drone Ports</ListSubheader>
            {dronePorts.map((d) => {
              const total = d.drones.length;
              const inUse = d.drones.filter((dr) => dr.inUse).length;
              return (
                <ListItem key={d.id || d.name} sx={{ width: '100%' }}>
                  <ListItemText
                    primary={`${d.name} (drones in use/total: ${inUse}/${total})`}
                    sx={wrapSx}
                  />
                </ListItem>
              );
            })}
          </>
        )}

      </List>
    </div>
  );
}

export default BuildingsPanel;
