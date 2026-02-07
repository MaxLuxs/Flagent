## @flagent/client@0.1.4

This generator creates TypeScript/JavaScript client that utilizes [axios](https://github.com/axios/axios). The generated Node module can be used in the following environments:

Environment
* Node.js
* Webpack
* Browserify

Language level
* ES5 - you must have a Promises/A+ library installed
* ES6

Module system
* CommonJS
* ES6 module system

It can be used in both TypeScript and JavaScript. In TypeScript, the definition will be automatically resolved via `package.json`. ([Reference](https://www.typescriptlang.org/docs/handbook/declaration-files/consumption.html))

### Building

To build and compile the typescript sources to javascript use:
```
npm install
npm run build
```

### Publishing

First build the package then run `npm publish`

### Consuming

navigate to the folder of your consuming project and run one of the following commands.

_published:_

```
npm install @flagent/client@0.1.4 --save
```

_unPublished (not recommended):_

```
npm install PATH_TO_GENERATED_PACKAGE --save
```

### Documentation for API Endpoints

All URIs are relative to *http://localhost:18000/api/v1*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*ConstraintApi* | [**createConstraint**](docs/ConstraintApi.md#createconstraint) | **POST** /flags/{flagId}/segments/{segmentId}/constraints | Create constraint
*ConstraintApi* | [**deleteConstraint**](docs/ConstraintApi.md#deleteconstraint) | **DELETE** /flags/{flagId}/segments/{segmentId}/constraints/{constraintId} | Delete constraint
*ConstraintApi* | [**findConstraints**](docs/ConstraintApi.md#findconstraints) | **GET** /flags/{flagId}/segments/{segmentId}/constraints | Get constraints for segment
*ConstraintApi* | [**putConstraint**](docs/ConstraintApi.md#putconstraint) | **PUT** /flags/{flagId}/segments/{segmentId}/constraints/{constraintId} | Update constraint
*DistributionApi* | [**findDistributions**](docs/DistributionApi.md#finddistributions) | **GET** /flags/{flagId}/segments/{segmentId}/distributions | Get distributions for segment
*DistributionApi* | [**putDistributions**](docs/DistributionApi.md#putdistributions) | **PUT** /flags/{flagId}/segments/{segmentId}/distributions | Update distributions
*EvaluationApi* | [**postEvaluation**](docs/EvaluationApi.md#postevaluation) | **POST** /evaluation | Evaluate flag
*EvaluationApi* | [**postEvaluationBatch**](docs/EvaluationApi.md#postevaluationbatch) | **POST** /evaluation/batch | Batch evaluate flags
*ExportApi* | [**getExportEvalCacheJSON**](docs/ExportApi.md#getexportevalcachejson) | **GET** /export/eval_cache/json | Export eval cache as JSON
*ExportApi* | [**getExportSQLite**](docs/ExportApi.md#getexportsqlite) | **GET** /export/sqlite | Export database as SQLite
*FlagApi* | [**createFlag**](docs/FlagApi.md#createflag) | **POST** /flags | Create a new flag
*FlagApi* | [**deleteFlag**](docs/FlagApi.md#deleteflag) | **DELETE** /flags/{flagId} | Delete flag
*FlagApi* | [**findFlags**](docs/FlagApi.md#findflags) | **GET** /flags | Get all flags
*FlagApi* | [**getFlag**](docs/FlagApi.md#getflag) | **GET** /flags/{flagId} | Get flag by ID
*FlagApi* | [**getFlagEntityTypes**](docs/FlagApi.md#getflagentitytypes) | **GET** /flags/entity_types | Get all entity types
*FlagApi* | [**getFlagSnapshots**](docs/FlagApi.md#getflagsnapshots) | **GET** /flags/{flagId}/snapshots | Get flag snapshots
*FlagApi* | [**putFlag**](docs/FlagApi.md#putflag) | **PUT** /flags/{flagId} | Update flag
*FlagApi* | [**restoreFlag**](docs/FlagApi.md#restoreflag) | **PUT** /flags/{flagId}/restore | Restore deleted flag
*FlagApi* | [**setFlagEnabled**](docs/FlagApi.md#setflagenabled) | **PUT** /flags/{flagId}/enabled | Set flag enabled status
*HealthApi* | [**getHealth**](docs/HealthApi.md#gethealth) | **GET** /health | Health check
*HealthApi* | [**getInfo**](docs/HealthApi.md#getinfo) | **GET** /info | Get version information
*SegmentApi* | [**createSegment**](docs/SegmentApi.md#createsegment) | **POST** /flags/{flagId}/segments | Create segment
*SegmentApi* | [**deleteSegment**](docs/SegmentApi.md#deletesegment) | **DELETE** /flags/{flagId}/segments/{segmentId} | Delete segment
*SegmentApi* | [**findSegments**](docs/SegmentApi.md#findsegments) | **GET** /flags/{flagId}/segments | Get segments for flag
*SegmentApi* | [**putSegment**](docs/SegmentApi.md#putsegment) | **PUT** /flags/{flagId}/segments/{segmentId} | Update segment
*SegmentApi* | [**putSegmentReorder**](docs/SegmentApi.md#putsegmentreorder) | **PUT** /flags/{flagId}/segments/reorder | Reorder segments
*TagApi* | [**createFlagTag**](docs/TagApi.md#createflagtag) | **POST** /flags/{flagId}/tags | Create tag and associate with flag
*TagApi* | [**deleteFlagTag**](docs/TagApi.md#deleteflagtag) | **DELETE** /flags/{flagId}/tags/{tagId} | Remove tag from flag
*TagApi* | [**findAllTags**](docs/TagApi.md#findalltags) | **GET** /tags | Get all tags
*TagApi* | [**findFlagTags**](docs/TagApi.md#findflagtags) | **GET** /flags/{flagId}/tags | Get tags for flag
*VariantApi* | [**createVariant**](docs/VariantApi.md#createvariant) | **POST** /flags/{flagId}/variants | Create variant
*VariantApi* | [**deleteVariant**](docs/VariantApi.md#deletevariant) | **DELETE** /flags/{flagId}/variants/{variantId} | Delete variant
*VariantApi* | [**findVariants**](docs/VariantApi.md#findvariants) | **GET** /flags/{flagId}/variants | Get variants for flag
*VariantApi* | [**putVariant**](docs/VariantApi.md#putvariant) | **PUT** /flags/{flagId}/variants/{variantId} | Update variant


### Documentation For Models

 - [Constraint](docs/Constraint.md)
 - [CreateConstraintRequest](docs/CreateConstraintRequest.md)
 - [CreateFlagRequest](docs/CreateFlagRequest.md)
 - [CreateSegmentRequest](docs/CreateSegmentRequest.md)
 - [CreateTagRequest](docs/CreateTagRequest.md)
 - [CreateVariantRequest](docs/CreateVariantRequest.md)
 - [Distribution](docs/Distribution.md)
 - [DistributionRequest](docs/DistributionRequest.md)
 - [EvalContext](docs/EvalContext.md)
 - [EvalDebugLog](docs/EvalDebugLog.md)
 - [EvalResult](docs/EvalResult.md)
 - [EvaluationBatchRequest](docs/EvaluationBatchRequest.md)
 - [EvaluationBatchResponse](docs/EvaluationBatchResponse.md)
 - [EvaluationEntity](docs/EvaluationEntity.md)
 - [Flag](docs/Flag.md)
 - [FlagSnapshot](docs/FlagSnapshot.md)
 - [Health](docs/Health.md)
 - [Info](docs/Info.md)
 - [ModelError](docs/Error.md)
 - [PutConstraintRequest](docs/PutConstraintRequest.md)
 - [PutDistributionsRequest](docs/PutDistributionsRequest.md)
 - [PutFlagRequest](docs/PutFlagRequest.md)
 - [PutSegmentReorderRequest](docs/PutSegmentReorderRequest.md)
 - [PutSegmentRequest](docs/PutSegmentRequest.md)
 - [PutVariantRequest](docs/PutVariantRequest.md)
 - [Segment](docs/Segment.md)
 - [SegmentDebugLog](docs/SegmentDebugLog.md)
 - [SetFlagEnabledRequest](docs/SetFlagEnabledRequest.md)
 - [Tag](docs/Tag.md)
 - [Variant](docs/Variant.md)


<a id="documentation-for-authorization"></a>
## Documentation For Authorization


Authentication schemes defined for the API:
<a id="bearerAuth"></a>
### bearerAuth

- **Type**: Bearer authentication (JWT)

<a id="basicAuth"></a>
### basicAuth

- **Type**: HTTP basic authentication

