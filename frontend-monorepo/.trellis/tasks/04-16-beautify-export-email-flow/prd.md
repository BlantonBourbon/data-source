# Beautify Export To Email Flow

## Goal

Improve the frontend UX for the existing "export selected rows and send email" flow in the data query screen.

## Requirements

- Keep the existing export and `/export/email` request behavior intact.
- Make the export-to-email controls feel intentional and easier to understand.
- Surface selection and recipient context inline instead of relying only on blocking alerts.
- Keep the changes scoped to the Angular frontend in the data query feature.

## Acceptance Criteria

- [ ] The export controls are visually grouped and clearer than the current inline form field + button.
- [ ] Users can see how many rows will be exported before sending.
- [ ] Users get inline feedback for empty selection, invalid/missing recipients, success, and failure.
- [ ] Existing CSV download and email-send behavior still works.

## Technical Notes

- Work in `projects/multi-module-app/src/app/features/data-query/`.
- Prefer existing Angular Material patterns already used by the app.
- Avoid backend API changes for this task.
