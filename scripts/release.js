#!/usr/bin/env node

const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

// Colors for console output
const colors = {
  reset: '\x1b[0m',
  bright: '\x1b[1m',
  red: '\x1b[31m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  magenta: '\x1b[35m',
  cyan: '\x1b[36m',
};

function log(message, color = colors.reset) {
  console.log(`${color}${message}${colors.reset}`);
}

function exec(command, options = {}) {
  try {
    return execSync(command, {
      stdio: 'inherit',
      encoding: 'utf8',
      ...options,
    });
  } catch (error) {
    log(`❌ Command failed: ${command}`, colors.red);
    process.exit(1);
  }
}

function getCurrentVersion() {
  const packageJson = JSON.parse(fs.readFileSync('package.json', 'utf8'));
  return packageJson.version;
}

function checkWorkingDirectory() {
  const packageJsonPath = path.join(process.cwd(), 'package.json');
  if (!fs.existsSync(packageJsonPath)) {
    log(
      '❌ package.json not found. Please run this script from the project root.',
      colors.red
    );
    process.exit(1);
  }
}

function checkGitStatus() {
  try {
    const status = execSync('git status --porcelain', { encoding: 'utf8' });
    if (status.trim()) {
      log(
        '❌ Working directory is not clean. Please commit or stash your changes.',
        colors.red
      );
      log('Uncommitted changes:', colors.yellow);
      console.log(status);
      process.exit(1);
    }
  } catch (error) {
    log('❌ Failed to check git status', colors.red);
    process.exit(1);
  }
}

function checkBranch() {
  try {
    const branch = execSync('git branch --show-current', {
      encoding: 'utf8',
    }).trim();
    if (branch !== 'main' && branch !== 'master') {
      log(
        `⚠️  You are on branch '${branch}'. Releases should typically be made from 'main' or 'master'.`,
        colors.yellow
      );
      const readline = require('readline');
      const rl = readline.createInterface({
        input: process.stdin,
        output: process.stdout,
      });

      return new Promise((resolve) => {
        rl.question('Do you want to continue? (y/N): ', (answer) => {
          rl.close();
          if (answer.toLowerCase() !== 'y' && answer.toLowerCase() !== 'yes') {
            log('Release cancelled.', colors.yellow);
            process.exit(0);
          }
          resolve();
        });
      });
    }
  } catch (error) {
    log('❌ Failed to check current branch', colors.red);
    process.exit(1);
  }
}

function runTests() {
  log('🧪 Running tests...', colors.blue);
  exec('yarn test');
  log('✅ Tests passed', colors.green);
}

function runLinting() {
  log('🔍 Running linting...', colors.blue);
  exec('yarn lint');
  log('✅ Linting passed', colors.green);
}

function runTypeCheck() {
  log('📝 Running type checking...', colors.blue);
  exec('yarn typecheck');
  log('✅ Type checking passed', colors.green);
}

function buildLibrary() {
  log('🏗️  Building library...', colors.blue);
  exec('yarn prepare');
  log('✅ Library built successfully', colors.green);
}

function publishToNpm(
  versionType,
  isPrerelease = false,
  prereleaseType = 'beta'
) {
  log(`📦 Publishing to npm...`, colors.blue);

  let releaseCommand = 'yarn release --ci';

  if (isPrerelease) {
    releaseCommand += ` --prerelease=${prereleaseType}`;
  } else {
    releaseCommand += ` --${versionType}`;
  }

  exec(releaseCommand);
  log('✅ Published to npm successfully', colors.green);
}

function showReleaseSummary(versionType, isPrerelease, prereleaseType) {
  const newVersion = getCurrentVersion();

  log('\n🎉 Release Summary:', colors.bright);
  log(`📦 Package: react-native-sportscar`, colors.cyan);
  log(`🏷️  Version: ${newVersion}`, colors.cyan);
  log(
    `📝 Type: ${isPrerelease ? `prerelease (${prereleaseType})` : versionType}`,
    colors.cyan
  );
  log(
    `🌐 NPM: https://www.npmjs.com/package/react-native-sportscar`,
    colors.cyan
  );
  log(
    `📚 GitHub: https://github.com/riteshshukla04/react-native-sportscar/releases/tag/v${newVersion}`,
    colors.cyan
  );

  log('\n📋 Next steps:', colors.bright);
  log('1. Verify the release on npm and GitHub', colors.yellow);
  log('2. Update any dependent projects', colors.yellow);
  log("3. Announce the release if it's a major version", colors.yellow);
}

async function main() {
  const args = process.argv.slice(2);
  const versionType = args[0] || 'patch';
  const isPrerelease = args.includes('--prerelease');
  const prereleaseType =
    args.find((arg) => arg.startsWith('--prerelease='))?.split('=')[1] ||
    'beta';
  const dryRun = args.includes('--dry-run');

  // Validate version type
  const validTypes = ['patch', 'minor', 'major'];
  if (!isPrerelease && !validTypes.includes(versionType)) {
    log(
      `❌ Invalid version type: ${versionType}. Valid types: ${validTypes.join(', ')}`,
      colors.red
    );
    process.exit(1);
  }

  log('🚀 React Native Sportscar Release Script', colors.bright);
  log('=====================================', colors.bright);

  // Pre-flight checks
  checkWorkingDirectory();
  checkGitStatus();
  await checkBranch();

  const currentVersion = getCurrentVersion();
  log(`📋 Current version: ${currentVersion}`, colors.cyan);
  log(
    `🎯 Release type: ${isPrerelease ? `prerelease (${prereleaseType})` : versionType}`,
    colors.cyan
  );

  if (dryRun) {
    log(
      '🧪 Running in dry-run mode - no actual changes will be made',
      colors.yellow
    );
  }

  // Quality checks
  runTests();
  runLinting();
  runTypeCheck();
  buildLibrary();

  if (dryRun) {
    log('🧪 Dry run completed successfully!', colors.green);
    log('All checks passed. Ready for release.', colors.green);
    return;
  }

  // Publish
  publishToNpm(versionType, isPrerelease, prereleaseType);

  // Show summary
  showReleaseSummary(versionType, isPrerelease, prereleaseType);
}

// Handle uncaught errors
process.on('uncaughtException', (error) => {
  log(`❌ Uncaught exception: ${error.message}`, colors.red);
  process.exit(1);
});

process.on('unhandledRejection', (reason) => {
  log(`❌ Unhandled rejection: ${reason}`, colors.red);
  process.exit(1);
});

// Run the script
main().catch((error) => {
  log(`❌ Release failed: ${error.message}`, colors.red);
  process.exit(1);
});
