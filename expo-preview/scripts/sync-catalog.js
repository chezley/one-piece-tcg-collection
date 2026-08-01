#!/usr/bin/env node
// Copies the card catalog from the iOS app (the source of truth) into the
// Expo preview so both apps show the same cards. Run after editing OP01.json.
const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');
const source = path.resolve(root, '..', 'OnePieceTCG', 'OnePieceTCG', 'Catalog');
const destination = path.join(root, 'src', 'data');

const files = fs.readdirSync(source).filter((name) => name.endsWith('.json'));

for (const file of files) {
  fs.copyFileSync(path.join(source, file), path.join(destination, file));
  console.log(`synced ${file}`);
}
