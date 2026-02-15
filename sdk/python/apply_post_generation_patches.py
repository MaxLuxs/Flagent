#!/usr/bin/env python3
"""
Post-generation patches for Flagent Python SDK (_generated).

The OpenAPI generator outputs code with TODOs for pydantic v2 and Python 3.
This script applies fixes so that:
- Models use model_dump_json(by_alias=True, exclude_unset=True) in to_json() (pydantic v2).
- api_client.py: remove TODO for 'long' type (Python 3 has no long, use int) and docstrings.

Run after generate.sh. If you regenerate the SDK, run:
  ./generate.sh
  python3 apply_post_generation_patches.py src/flagent/_generated
"""
import re
import sys
from pathlib import Path


def patch_models_to_json(generated_dir: Path) -> None:
    """Replace to_json() TODO + json.dumps(self.to_dict()) with model_dump_json in all models."""
    models_dir = generated_dir / "models"
    if not models_dir.is_dir():
        return
    pattern = re.compile(
        r"\s+# TODO: pydantic v2: use \.model_dump_json\(by_alias=True, exclude_unset=True\) instead\n(\s+)return json\.dumps\(self\.to_dict\(\)\)",
        re.MULTILINE,
    )
    replacement = r"\1return self.model_dump_json(by_alias=True, exclude_unset=True)"
    for path in models_dir.glob("*.py"):
        if path.name == "__init__.py":
            continue
        text = path.read_text(encoding="utf-8")
        if "TODO: pydantic v2" in text:
            new_text = pattern.sub(replacement, text)
            if new_text != text:
                path.write_text(new_text, encoding="utf-8")
                print(f"Patched: {path.relative_to(generated_dir)}")


def patch_api_client(generated_dir: Path) -> None:
    """Fix api_client.py: long TODO and docstrings (Python 3: long is int)."""
    path = generated_dir / "api_client.py"
    if not path.is_file():
        return
    text = path.read_text(encoding="utf-8")
    original = text
    text = text.replace(
        "'long': int, # TODO remove as only py3 is supported?",
        "'long': int,  # Python 3: long unified with int",
    )
    text = re.sub(
        r"If obj is str, int, long, float, bool,",
        "If obj is str, int, float, bool,",
        text,
    )
    text = re.sub(
        r":return: int, long, float, str, bool\.\n",
        ":return: int, float, str, bool.\n",
        text,
    )
    if text != original:
        path.write_text(text, encoding="utf-8")
        print(f"Patched: {path.relative_to(generated_dir)}")


def main() -> None:
    if len(sys.argv) < 2:
        generated = Path(__file__).resolve().parent / "src" / "flagent" / "_generated"
    else:
        generated = Path(sys.argv[1]).resolve()
    if not generated.is_dir():
        print(f"Error: directory not found: {generated}", file=sys.stderr)
        sys.exit(1)
    patch_models_to_json(generated)
    patch_api_client(generated)
    print("Post-generation patches applied.")


if __name__ == "__main__":
    main()
