#!/usr/bin/env python3

from __future__ import annotations

import os
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def find_test_xml_files(root: Path) -> list[Path]:
    test_dirs = sorted(
        path for path in root.rglob("build/test-results") if path.is_dir()
    )
    test_xml_files: list[Path] = []
    for test_dir in test_dirs:
        test_xml_files.extend(sorted(test_dir.rglob("*.xml")))
    return test_xml_files


def find_kover_xml_files(root: Path) -> list[Path]:
    report_xml_files = sorted(
        path
        for path in root.rglob("report.xml")
        if path.is_file() and "build" in path.parts and "reports" in path.parts and "kover" in path.parts
    )
    if report_xml_files:
        return report_xml_files

    kover_dirs = sorted(
        path for path in root.rglob("build/reports/kover") if path.is_dir()
    )
    kover_xml_files: list[Path] = []
    for kover_dir in kover_dirs:
        kover_xml_files.extend(sorted(kover_dir.rglob("*.xml")))
    return kover_xml_files


def parse_testsuite_attributes(root: ET.Element) -> dict[str, int]:
    if root.tag == "testsuite":
        return {
            "tests": int(root.attrib.get("tests", 0)),
            "failures": int(root.attrib.get("failures", 0)),
            "errors": int(root.attrib.get("errors", 0)),
            "skipped": int(root.attrib.get("skipped", 0)),
        }

    if root.tag == "testsuites":
        suites = root.findall("testsuite")
        if suites:
            aggregated = {"tests": 0, "failures": 0, "errors": 0, "skipped": 0}
            for suite in suites:
                suite_counts = parse_testsuite_attributes(suite)
                for key in aggregated:
                    aggregated[key] += suite_counts[key]
            return aggregated

        return {
            "tests": int(root.attrib.get("tests", 0)),
            "failures": int(root.attrib.get("failures", 0)),
            "errors": int(root.attrib.get("errors", 0)),
            "skipped": int(root.attrib.get("skipped", 0)),
        }

    return {"tests": 0, "failures": 0, "errors": 0, "skipped": 0}


def aggregate_test_results(xml_files: list[Path]) -> dict[str, int]:
    totals = {"tests": 0, "failures": 0, "errors": 0, "skipped": 0}
    for xml_file in xml_files:
        try:
            root = ET.parse(xml_file).getroot()
        except ET.ParseError:
            continue

        suite_counts = parse_testsuite_attributes(root)
        for key in totals:
            totals[key] += suite_counts[key]

    totals["failed"] = totals["failures"] + totals["errors"]
    totals["passed"] = max(totals["tests"] - totals["failed"] - totals["skipped"], 0)
    return totals


def parse_kover_line_coverage(xml_files: list[Path]) -> tuple[str, Path | None]:
    for xml_file in xml_files:
        try:
            root = ET.parse(xml_file).getroot()
        except ET.ParseError:
            continue

        line_counter = root.find("./counter[@type='LINE']")
        if line_counter is None:
            line_counter = root.find(".//counter[@type='LINE']")
        if line_counter is None:
            continue

        covered = int(line_counter.attrib.get("covered", 0))
        missed = int(line_counter.attrib.get("missed", 0))
        total = covered + missed
        if total == 0:
            return "Not available", xml_file

        coverage = round((covered / total) * 100, 1)
        return f"{coverage:.1f}%", xml_file

    return "Not available", None


def write_summary(summary_path: Path, totals: dict[str, int], coverage: str, missing_test_xml: bool) -> None:
    lines = [
        "## Tests Summary",
        "",
        "| Metric | Value |",
        "|---|---:|",
        f"| Total tests | {totals['tests']} |",
        f"| Passed | {totals['passed']} |",
        f"| Failed | {totals['failed']} |",
        f"| Skipped | {totals['skipped']} |",
        f"| Line coverage | {coverage} |",
    ]

    if missing_test_xml:
        lines.extend(["", "Test XML files were not found in the expected Gradle report locations."])

    summary_path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main() -> int:
    summary_path_value = os.environ.get("GITHUB_STEP_SUMMARY")
    if not summary_path_value:
        print("GITHUB_STEP_SUMMARY is required", file=sys.stderr)
        return 1

    tests_exit_code = int(os.environ.get("TESTS_EXIT_CODE", "0"))
    root = Path.cwd()
    summary_path = Path(summary_path_value)

    test_xml_files = find_test_xml_files(root)
    totals = aggregate_test_results(test_xml_files)
    missing_test_xml = len(test_xml_files) == 0

    kover_xml_files = find_kover_xml_files(root)
    coverage, _coverage_path = parse_kover_line_coverage(kover_xml_files)

    write_summary(summary_path, totals, coverage, missing_test_xml)

    if missing_test_xml and tests_exit_code == 0:
        return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
