package flagent

import "fmt"

// FlagentError is the base error type for all Flagent errors
type FlagentError struct {
	Message string
	Err     error
}

func (e *FlagentError) Error() string {
	if e.Err != nil {
		return fmt.Sprintf("%s: %v", e.Message, e.Err)
	}
	return e.Message
}

func (e *FlagentError) Unwrap() error {
	return e.Err
}

// FlagNotFoundError indicates that the requested flag was not found
type FlagNotFoundError struct {
	FlagentError
}

// EvaluationError indicates that flag evaluation failed
type EvaluationError struct {
	FlagentError
}

// NetworkError indicates a network-related error
type NetworkError struct {
	FlagentError
}

// InvalidConfigError indicates invalid configuration
type InvalidConfigError struct {
	FlagentError
}

// NewFlagNotFoundError creates a new FlagNotFoundError
func NewFlagNotFoundError(message string, err error) *FlagNotFoundError {
	return &FlagNotFoundError{
		FlagentError: FlagentError{Message: message, Err: err},
	}
}

// NewEvaluationError creates a new EvaluationError
func NewEvaluationError(message string, err error) *EvaluationError {
	return &EvaluationError{
		FlagentError: FlagentError{Message: message, Err: err},
	}
}

// NewNetworkError creates a new NetworkError
func NewNetworkError(message string, err error) *NetworkError {
	return &NetworkError{
		FlagentError: FlagentError{Message: message, Err: err},
	}
}

// NewInvalidConfigError creates a new InvalidConfigError
func NewInvalidConfigError(message string, err error) *InvalidConfigError {
	return &InvalidConfigError{
		FlagentError: FlagentError{Message: message, Err: err},
	}
}
