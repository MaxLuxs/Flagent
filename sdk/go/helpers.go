package flagent

// StringPtr returns a pointer to the given string value
func StringPtr(s string) *string {
	return &s
}

// Int64Ptr returns a pointer to the given int64 value
func Int64Ptr(i int64) *int64 {
	return &i
}

// BoolPtr returns a pointer to the given bool value
func BoolPtr(b bool) *bool {
	return &b
}

// IntPtr returns a pointer to the given int value
func IntPtr(i int) *int {
	return &i
}
