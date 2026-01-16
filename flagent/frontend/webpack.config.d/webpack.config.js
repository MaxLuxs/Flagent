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
    
    // CRITICAL: Create plugin to expose modules as globals
    config.plugins = config.plugins || [];
    
    class ExposeKotlinModulesPlugin {
        apply(compiler) {
            compiler.hooks.compilation.tap('ExposeKotlinModulesPlugin', (compilation) => {
                compilation.hooks.processAssets.tap(
                    {
                        name: 'ExposeKotlinModulesPlugin',
                        stage: compilation.PROCESS_ASSETS_STAGE_OPTIMIZE
                    },
                    (assets) => {
                        // After webpack processes modules, inject code to expose them as globals
                        Object.keys(assets).forEach(filename => {
                            if (filename.endsWith('.js') && !filename.endsWith('.map')) {
                                const asset = assets[filename];
                                let source = asset.source();
                                
                                // Extract module names from webpack's module registry
                                // and create global variables for UMD fallback
                                const moduleExports = [];
                                const moduleRegex = /__webpack_require__\.r\(exports\);/g;
                                
                                // Find all module exports and create globals
                                // This is a simplified approach - we'll inject code at the end
                                const injectCode = `
// Expose Kotlin modules as globals for UMD fallback
(function() {
    if (typeof __webpack_require__ !== 'undefined' && __webpack_require__.cache) {
        const moduleMap = {
            './kotlin/ktor-ktor-client-content-negotiation.js': 'ktor-ktor-client-content-negotiation',
            './kotlin/kotlin-kotlin-stdlib.js': 'kotlin-kotlin-stdlib',
            './kotlin/ktor-ktor-http.js': 'ktor-ktor-http',
            './kotlin/ktor-ktor-utils.js': 'ktor-ktor-utils',
            './kotlin/ktor-ktor-client-core.js': 'ktor-ktor-client-core',
            './kotlin/ktor-ktor-serialization.js': 'ktor-ktor-serialization',
            './kotlin/ktor-ktor-io.js': 'ktor-ktor-io',
            './kotlin/kotlinx-serialization-kotlinx-serialization-json.js': 'kotlinx-serialization-kotlinx-serialization-json',
            './kotlin/ktor-ktor-serialization-kotlinx-json.js': 'ktor-ktor-serialization-kotlinx-json',
            './kotlin/flagent-shared.js': 'flagent-shared',
            './kotlin/compose-multiplatform-core-compose-runtime-runtime.js': 'compose-multiplatform-core-compose-runtime-runtime',
            './kotlin/html-html-core.js': 'html-html-core',
            './kotlin/kotlinx-coroutines-core.js': 'kotlinx-coroutines-core',
            './kotlin/html-internal-html-core-runtime.js': 'html-internal-html-core-runtime'
        };
        
        Object.keys(moduleMap).forEach(modulePath => {
            const globalName = moduleMap[modulePath];
            try {
                const module = __webpack_require__(modulePath);
                if (module && typeof this !== 'undefined') {
                    this[globalName] = module;
                }
            } catch (e) {
                // Module not found, skip
            }
        });
    }
})();
`;
                                
                                // Inject at the beginning, before any module code runs
                                source = injectCode + source;
                                compilation.updateAsset(filename, {
                                    source: () => source,
                                    size: () => source.length
                                });
                            }
                        });
                    }
                );
            });
        }
    }
    
    config.plugins.push(new ExposeKotlinModulesPlugin());
    
    // Fix dev server static paths
    const kotlinSourceDir = path.resolve(__dirname, '../../build/js/packages/flagent-frontend/kotlin');
    
    if (!config.devServer) {
        config.devServer = {};
    }
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
