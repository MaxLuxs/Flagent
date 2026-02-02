;(function(config) {
    const path = require('path');
    const webpack = require('webpack');
    
    // CRITICAL FIX: Kotlin/JS UMD modules check for globals this['module-name']
    // but webpack uses AMD/CommonJS. We need to expose modules as globals.
    
    // Don't treat ANY Kotlin JS modules as externals
    config.externals = config.externals || {};
    
    const kotlinModulePatterns = [
        /^ktor-ktor-/,
        /^kotlin-kotlin-/,
        /^kotlinx-/,
        /^flagent-/,
        /^compose-/,
        /^html-/
    ];
    
    const originalExternals = config.externals;
    if (typeof originalExternals === 'function') {
        config.externals = function(context, request, callback) {
            const isKotlinModule = kotlinModulePatterns.some(pattern => 
                pattern.test(request) || request.includes('./kotlin/')
            );
            if (isKotlinModule) {
                return callback();
            }
            if (typeof originalExternals === 'function') {
                return originalExternals(context, request, callback);
            }
            return callback();
        };
    } else if (Array.isArray(originalExternals)) {
        config.externals = originalExternals.filter(ext => {
            if (typeof ext === 'string') {
                return !kotlinModulePatterns.some(pattern => pattern.test(ext));
            }
            return true;
        });
    } else if (typeof originalExternals === 'object') {
        Object.keys(originalExternals).forEach(key => {
            if (kotlinModulePatterns.some(pattern => pattern.test(key))) {
                delete config.externals[key];
            }
        });
    }
    
    config.optimization = config.optimization || {};
    config.optimization.splitChunks = false;
    config.optimization.minimize = false; // Disable minification to avoid terser errors
    
    // Disable source maps for production to avoid SourceMapDevToolPlugin errors
    if (config.devtool) {
        delete config.devtool;
    }
    config.devtool = false;
    
    // Remove SourceMapDevToolPlugin if present
    if (config.plugins) {
        config.plugins = config.plugins.filter(plugin => {
            return plugin && plugin.constructor && plugin.constructor.name !== 'SourceMapDevToolPlugin';
        });
    }
    
    // CRITICAL: Create plugin to expose modules as globals
    config.plugins = config.plugins || [];
    
    // Simplified: Don't modify assets in plugin to avoid source map issues
    // Kotlin/JS modules should work without global exposure in webpack
    
    // Fix dev server static paths
    const kotlinSourceDir = path.resolve(__dirname, '../../build/js/packages/flagent-frontend/kotlin');
    
    if (!config.devServer) {
        config.devServer = {};
    }
    // SPA: serve index.html for all routes (e.g. /login, /tenants) so client-side router works after full page load
    config.devServer.historyApiFallback = true;
    if (!config.devServer.static) {
        config.devServer.static = [];
    }
    
    const hasKotlin = config.devServer.static.some(s => {
        if (typeof s === 'string') return s.includes('kotlin');
        if (typeof s === 'object' && s.directory) {
            return s.directory.includes('kotlin') || s.publicPath === '/kotlin';
        }
        return false;
    });
    
    if (!hasKotlin) {
        config.devServer.static.push({
            directory: kotlinSourceDir,
            publicPath: '/kotlin',
            serveIndex: false,
            watch: false
        });
    }
    
    if (!config.resolve) {
        config.resolve = {};
    }
    if (!config.resolve.modules) {
        config.resolve.modules = ["node_modules"];
    }
    
    if (config.resolve.modules.indexOf(kotlinSourceDir) === -1) {
        config.resolve.modules.push(kotlinSourceDir);
    }
    
    config.resolve.alias = config.resolve.alias || {};
})(config);
